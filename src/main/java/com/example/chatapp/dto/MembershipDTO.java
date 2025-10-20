package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MembershipDTO(
    Long id,
    UserDTO user,
    UUID serverId,
    String nickname,
    LocalDateTime joinedAt
) {
    
}
