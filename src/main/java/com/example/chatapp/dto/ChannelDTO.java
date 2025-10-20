package com.example.chatapp.dto;

import java.util.UUID;

import com.example.chatapp.enums.ChannelType;

public record ChannelDTO(
    UUID id,
    UUID serverId,
    String name,
    ChannelType type
) {
    
}
