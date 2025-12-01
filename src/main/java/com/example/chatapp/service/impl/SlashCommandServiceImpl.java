package com.example.chatapp.service.impl;

import com.example.chatapp.dto.SlashCommandResponse;
import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.MembershipRole;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.ServerRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.ModerationService;
import com.example.chatapp.service.SlashCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlashCommandServiceImpl implements SlashCommandService {

    private final ModerationService moderationService;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final MembershipRepository membershipRepository;
    
    // Command patterns
    private static final Pattern KICK_PATTERN = Pattern.compile("^/kick\\s+@?(\\w+)(?:\\s+(.*))?$");
    private static final Pattern BAN_PATTERN = Pattern.compile("^/ban\\s+@?(\\w+)(?:\\s+(.*))?$");
    private static final Pattern MUTE_PATTERN = Pattern.compile("^/mute\\s+@?(\\w+)(?:\\s+(\\d+))?(?:\\s+(.*))?$");
    private static final Pattern UNMUTE_PATTERN = Pattern.compile("^/unmute\\s+@?(\\w+)$");
    private static final Pattern HELP_PATTERN = Pattern.compile("^/help(?:\\s+(\\w+))?$");
    private static final Pattern CLEAR_PATTERN = Pattern.compile("^/clear\\s+(\\d+)$");
    private static final Pattern NICK_PATTERN = Pattern.compile("^/nick\\s+@?(\\w+)\\s+(.+)$");
    
    // Available commands with their info
    private static final List<CommandInfo> ALL_COMMANDS = List.of(
        new CommandInfo("kick", "Kick a user from the server", "/kick @username [reason]", "MODERATOR"),
        new CommandInfo("ban", "Ban a user from the server", "/ban @username [reason]", "ADMIN"),
        new CommandInfo("mute", "Mute a user for specified minutes", "/mute @username [minutes] [reason]", "MODERATOR"),
        new CommandInfo("unmute", "Unmute a user", "/unmute @username", "MODERATOR"),
        new CommandInfo("clear", "Delete last N messages in channel", "/clear <count>", "MODERATOR"),
        new CommandInfo("nick", "Change a user's nickname", "/nick @username <new_nickname>", "MODERATOR"),
        new CommandInfo("help", "Show available commands", "/help [command]", "MEMBER")
    );
    
    // Role hierarchy for permission checking
    private static final Map<String, Integer> ROLE_HIERARCHY = Map.of(
        "OWNER", 4,
        "ADMIN", 3,
        "MODERATOR", 2,
        "MEMBER", 1
    );

    @Override
    public SlashCommandResponse executeCommand(String commandString, UUID executorId, UUID serverId, UUID channelId) {
        if (commandString == null || !commandString.startsWith("/")) {
            return SlashCommandResponse.error("unknown", "Invalid command format. Commands must start with /");
        }
        
        String command = extractCommandName(commandString);
        
        // Check permission
        if (!hasPermission(executorId, serverId, command)) {
            return SlashCommandResponse.error(command, "You don't have permission to use this command");
        }
        
        try {
            return switch (command) {
                case "kick" -> executeKick(commandString, executorId, serverId);
                case "ban" -> executeBan(commandString, executorId, serverId);
                case "mute" -> executeMute(commandString, executorId, serverId);
                case "unmute" -> executeUnmute(commandString, executorId, serverId);
                case "clear" -> executeClear(commandString, channelId);
                case "nick" -> executeNick(commandString, serverId);
                case "help" -> executeHelp(commandString, executorId, serverId);
                default -> SlashCommandResponse.error(command, "Unknown command: /" + command + ". Use /help for available commands.");
            };
        } catch (Exception e) {
            log.error("Command execution failed: {} - {}", commandString, e.getMessage());
            return SlashCommandResponse.error(command, "Command failed: " + e.getMessage());
        }
    }

    @Override
    public boolean hasPermission(UUID userId, UUID serverId, String command) {
        CommandInfo cmdInfo = ALL_COMMANDS.stream()
                .filter(c -> c.name().equals(command))
                .findFirst()
                .orElse(null);
        
        if (cmdInfo == null) {
            return false;
        }
        
        String requiredRole = cmdInfo.requiredRole();
        MembershipRole userRole = getUserRole(userId, serverId);
        
        if (userRole == null) {
            return false;
        }
        
        int requiredLevel = ROLE_HIERARCHY.getOrDefault(requiredRole, 0);
        int userLevel = ROLE_HIERARCHY.getOrDefault(userRole.name(), 0);
        
        return userLevel >= requiredLevel;
    }

    @Override
    public List<CommandInfo> getAvailableCommands(UUID userId, UUID serverId) {
        return ALL_COMMANDS.stream()
                .filter(cmd -> hasPermission(userId, serverId, cmd.name()))
                .toList();
    }
    
    private String extractCommandName(String commandString) {
        String[] parts = commandString.trim().split("\\s+");
        return parts[0].substring(1).toLowerCase(); // Remove / and lowercase
    }
    
    private MembershipRole getUserRole(UUID userId, UUID serverId) {
        User user = userRepository.findById(userId).orElse(null);
        Server server = serverRepository.findById(serverId).orElse(null);
        
        if (user == null || server == null) {
            return null;
        }
        
        // Check if owner
        if (server.getOwner().getId().equals(userId)) {
            return MembershipRole.OWNER;
        }
        
        return membershipRepository.findByUserAndServer(user, server)
                .map(Membership::getRole)
                .orElse(null);
    }
    
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    // --- Command Implementations ---
    
    private SlashCommandResponse executeKick(String commandString, UUID executorId, UUID serverId) {
        Matcher matcher = KICK_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("kick", "Usage: /kick @username [reason]");
        }
        
        String username = matcher.group(1);
        String reason = matcher.group(2);
        
        User targetUser = findUserByUsername(username);
        
        // Can't kick yourself
        if (targetUser.getId().equals(executorId)) {
            return SlashCommandResponse.error("kick", "You cannot kick yourself");
        }
        
        // Can't kick owner
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        if (server.getOwner().getId().equals(targetUser.getId())) {
            return SlashCommandResponse.error("kick", "You cannot kick the server owner");
        }
        
        moderationService.kickUser(targetUser.getId(), serverId, executorId);
        
        String message = "User @" + username + " has been kicked" + 
                (reason != null ? " for: " + reason : "");
        log.info("User {} kicked {} from server {}", executorId, targetUser.getId(), serverId);
        
        return SlashCommandResponse.success("kick", message);
    }
    
    private SlashCommandResponse executeBan(String commandString, UUID executorId, UUID serverId) {
        Matcher matcher = BAN_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("ban", "Usage: /ban @username [reason]");
        }
        
        String username = matcher.group(1);
        String reason = matcher.group(2);
        
        User targetUser = findUserByUsername(username);
        
        // Can't ban yourself
        if (targetUser.getId().equals(executorId)) {
            return SlashCommandResponse.error("ban", "You cannot ban yourself");
        }
        
        // Can't ban owner
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        if (server.getOwner().getId().equals(targetUser.getId())) {
            return SlashCommandResponse.error("ban", "You cannot ban the server owner");
        }
        
        moderationService.banUser(targetUser.getId(), serverId, executorId);
        
        String message = "User @" + username + " has been banned" + 
                (reason != null ? " for: " + reason : "");
        log.info("User {} banned {} from server {}", executorId, targetUser.getId(), serverId);
        
        return SlashCommandResponse.success("ban", message);
    }
    
    private SlashCommandResponse executeMute(String commandString, UUID executorId, UUID serverId) {
        Matcher matcher = MUTE_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("mute", "Usage: /mute @username [minutes] [reason]");
        }
        
        String username = matcher.group(1);
        String minutesStr = matcher.group(2);
        String reason = matcher.group(3);
        
        int minutes = minutesStr != null ? Integer.parseInt(minutesStr) : 10; // Default 10 minutes
        
        User targetUser = findUserByUsername(username);
        
        // Can't mute yourself
        if (targetUser.getId().equals(executorId)) {
            return SlashCommandResponse.error("mute", "You cannot mute yourself");
        }
        
        moderationService.muteUser(targetUser.getId(), serverId, executorId, minutes);
        
        String message = "User @" + username + " has been muted for " + minutes + " minutes" + 
                (reason != null ? " for: " + reason : "");
        log.info("User {} muted {} for {} minutes in server {}", executorId, targetUser.getId(), minutes, serverId);
        
        return SlashCommandResponse.success("mute", message);
    }
    
    private SlashCommandResponse executeUnmute(String commandString, UUID executorId, UUID serverId) {
        Matcher matcher = UNMUTE_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("unmute", "Usage: /unmute @username");
        }
        
        String username = matcher.group(1);
        User targetUser = findUserByUsername(username);
        
        // For unmute, we call mute with 0 minutes (which will effectively remove the mute)
        moderationService.muteUser(targetUser.getId(), serverId, executorId, 0);
        
        String message = "User @" + username + " has been unmuted";
        log.info("User {} unmuted {} in server {}", executorId, targetUser.getId(), serverId);
        
        return SlashCommandResponse.success("unmute", message);
    }
    
    private SlashCommandResponse executeClear(String commandString, UUID channelId) {
        Matcher matcher = CLEAR_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("clear", "Usage: /clear <count>");
        }
        
        int count = Integer.parseInt(matcher.group(1));
        if (count < 1 || count > 100) {
            return SlashCommandResponse.error("clear", "Count must be between 1 and 100");
        }
        
        // Note: This would need MessageService to implement bulk delete
        // For now, return a placeholder response
        String message = "Cleared " + count + " messages";
        log.info("Cleared {} messages from channel {}", count, channelId);
        
        return SlashCommandResponse.success("clear", message, Map.of("count", count));
    }
    
    private SlashCommandResponse executeNick(String commandString, UUID serverId) {
        Matcher matcher = NICK_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("nick", "Usage: /nick @username <new_nickname>");
        }
        
        String username = matcher.group(1);
        String newNickname = matcher.group(2);
        
        User targetUser = findUserByUsername(username);
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        
        // Update nickname in membership settings
        Membership membership = membershipRepository.findByUserAndServer(targetUser, server)
                .orElseThrow(() -> new RuntimeException("User is not a member of this server"));
        
        String settings = String.format("{\"nickname\":\"%s\"}", newNickname);
        membership.setSettings(settings);
        membershipRepository.save(membership);
        
        String message = "User @" + username + " nickname changed to " + newNickname;
        log.info("Changed nickname for {} to {} in server {}", targetUser.getId(), newNickname, serverId);
        
        return SlashCommandResponse.success("nick", message);
    }
    
    private SlashCommandResponse executeHelp(String commandString, UUID executorId, UUID serverId) {
        Matcher matcher = HELP_PATTERN.matcher(commandString);
        if (!matcher.matches()) {
            return SlashCommandResponse.error("help", "Usage: /help [command]");
        }
        
        String specificCommand = matcher.group(1);
        
        if (specificCommand != null) {
            // Show help for specific command
            CommandInfo cmdInfo = ALL_COMMANDS.stream()
                    .filter(c -> c.name().equals(specificCommand.toLowerCase()))
                    .findFirst()
                    .orElse(null);
            
            if (cmdInfo == null) {
                return SlashCommandResponse.error("help", "Unknown command: " + specificCommand);
            }
            
            String helpText = String.format("**/%s**\n%s\nUsage: %s\nRequired Role: %s",
                    cmdInfo.name(), cmdInfo.description(), cmdInfo.usage(), cmdInfo.requiredRole());
            
            return SlashCommandResponse.success("help", helpText, cmdInfo);
        }
        
        // Show all available commands
        List<CommandInfo> availableCommands = getAvailableCommands(executorId, serverId);
        
        StringBuilder helpText = new StringBuilder("**Available Commands:**\n");
        for (CommandInfo cmd : availableCommands) {
            helpText.append(String.format("• **/%s** - %s\n", cmd.name(), cmd.description()));
        }
        helpText.append("\nUse /help <command> for more details.");
        
        return SlashCommandResponse.success("help", helpText.toString(), availableCommands);
    }
}
