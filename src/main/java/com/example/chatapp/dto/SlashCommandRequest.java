package com.example.chatapp.dto;

public record SlashCommandRequest(
    String command,     // Full command string like "/kick @username reason"
    String channelId,
    String serverId
) {}
