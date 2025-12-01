package com.example.chatapp.dto;

import java.util.UUID;

public record CreateInviteRequest(
    UUID serverId,
    Integer maxUses,        // null = unlimited
    Integer expiresInHours  // null = never expires
) {}
