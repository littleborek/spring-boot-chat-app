package com.example.chatapp.dto;

public record SlashCommandResponse(
    boolean success,
    String command,
    String message,
    Object data
) {
    public static SlashCommandResponse success(String command, String message) {
        return new SlashCommandResponse(true, command, message, null);
    }
    
    public static SlashCommandResponse success(String command, String message, Object data) {
        return new SlashCommandResponse(true, command, message, data);
    }
    
    public static SlashCommandResponse error(String command, String message) {
        return new SlashCommandResponse(false, command, message, null);
    }
}
