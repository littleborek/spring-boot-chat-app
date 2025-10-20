package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MessageDTO(
    Long id,
    UUID channelId,
    userDTO author,
    String content,
    LocalDateTime createdAt,
    LocalDateTime editedAt,
    Long replyToMessageId,
    List<AttachmentDTO> attachments
) {
    
}
