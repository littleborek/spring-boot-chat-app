package com.example.chatapp.dto;


import com.example.chatapp.enums.ChannelType;

public record CreateChannelRequest(
    String name,
    ChannelType type
) {
    
}
