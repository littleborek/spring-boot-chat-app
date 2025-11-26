package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatapp.enums.NotificationType;

public record NotificationDTO(
    UUID id,
    UUID recipientId,
    UUID senderId,
    NotificationType type,
    Long messageId,
    UUID channelId,
    UUID serverId,
    boolean isRead,
    LocalDateTime createdAt
) {
    
}
