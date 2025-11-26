package com.example.chatapp.dto;

import java.util.UUID;

public record ServerDTO(
    UUID id,
    UUID ownerId,
    String name,
    String description
) {

    
}
