package com.example.chatapp.dto;

import java.util.UUID;

public record AttachmentDTO(
    UUID id,
    String storageKey,
    String mimeType,
    Integer size
) {
    
}
