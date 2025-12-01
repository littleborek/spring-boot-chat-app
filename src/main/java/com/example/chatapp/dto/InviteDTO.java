package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InviteDTO(
    UUID id,
    String code,
    UUID serverId,
    String serverName,
    UUID createdById,
    String createdByUsername,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    Integer maxUses,
    Integer currentUses,
    boolean isActive
) {}
