package com.example.chatapp.dto;

public record UpdateProfileRequest(
    String username,
    String avatarUrl,
    String profileMeta
) {}
