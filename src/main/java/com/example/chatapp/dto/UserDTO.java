package com.example.chatapp.dto;

import java.util.UUID;



public record UserDTO(
    UUID id,
    String username,
    String avatarurl
) {}
