package com.example.chatapp.dto;

public record CreateServerRequest(
    String name,
    String description,
    String settings
) {
    
}
