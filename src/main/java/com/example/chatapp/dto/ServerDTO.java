package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServerDTO(
    UUID id,
    UUID ownerId,
    String name,
    String description,
    LocalDateTime createdAt
) {
    
}
