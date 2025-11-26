package com.example.chatapp.service.impl;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.pattern.command.*;
import com.example.chatapp.repository.*;
import com.example.chatapp.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation that uses Command Pattern for moderation actions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {
    
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MembershipRepository membershipRepository;
    private final MessageRepository messageRepository;
    private final CommandInvoker commandInvoker;
    
    @Override
    @Transactional
    public void kickUser(UUID targetUserId, UUID serverId, UUID moderatorId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        // Check if moderator is server owner
        validateServerOwner(moderatorId, server);
        
        // Create and execute kick command
        Command kickCommand = new KickUserCommand(targetUser, server, membershipRepository);
        commandInvoker.executeCommand(kickCommand);
    }
    
    @Override
    @Transactional
    public void banUser(UUID targetUserId, UUID serverId, UUID moderatorId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        validateServerOwner(moderatorId, server);
        
        // Create and execute ban command
        Command banCommand = new BanUserCommand(targetUser, server, membershipRepository);
        commandInvoker.executeCommand(banCommand);
    }
    
    @Override
    @Transactional
    public void muteUser(UUID targetUserId, UUID serverId, UUID moderatorId, int durationMinutes) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        validateServerOwner(moderatorId, server);
        
        Membership membership = membershipRepository.findByUserAndServer(targetUser, server)
                .orElseThrow(() -> new BadRequestException("User is not a member of this server"));
        
        // Create and execute mute command
        Command muteCommand = new MuteUserCommand(targetUser, membership, membershipRepository, durationMinutes);
        commandInvoker.executeCommand(muteCommand);
    }
    
    @Override
    @Transactional
    public void deleteMessage(Long messageId, UUID moderatorId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BadRequestException("Message not found"));
        
        Server server = message.getChannel().getServer();
        validateServerOwner(moderatorId, server);
        
        // Create and execute delete command
        Command deleteCommand = new DeleteMessageCommand(message, messageRepository);
        commandInvoker.executeCommand(deleteCommand);
    }
    
    @Override
    @Transactional
    public void joinChannel(UUID userId, UUID channelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BadRequestException("Channel not found"));
        
        // Create and execute join command
        Command joinCommand = new JoinChannelCommand(user, channel, membershipRepository);
        commandInvoker.executeCommand(joinCommand);
    }
    
    private void validateServerOwner(UUID userId, Server server) {
        if (!server.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Only server owner can perform this action");
        }
    }
}
