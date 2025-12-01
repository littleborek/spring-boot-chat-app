package com.example.chatapp.service;

import com.example.chatapp.dto.SlashCommandResponse;

import java.util.List;
import java.util.UUID;

public interface SlashCommandService {
    
    /**
     * Parse and execute a slash command
     */
    SlashCommandResponse executeCommand(String commandString, UUID executorId, UUID serverId, UUID channelId);
    
    /**
     * Check if user has permission to execute a command
     */
    boolean hasPermission(UUID userId, UUID serverId, String command);
    
    /**
     * Get list of available commands for a user
     */
    List<CommandInfo> getAvailableCommands(UUID userId, UUID serverId);
    
    /**
     * Command information record
     */
    record CommandInfo(
        String name,
        String description,
        String usage,
        String requiredRole
    ) {}
}
