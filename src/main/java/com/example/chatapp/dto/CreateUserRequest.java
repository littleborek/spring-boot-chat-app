package com.example.chatapp.dto;

public record CreateUserRequest(
    String username,
    String email,
    String password
    ) {}
