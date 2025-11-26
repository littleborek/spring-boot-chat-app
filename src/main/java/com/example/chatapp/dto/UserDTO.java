package com.example.chatapp.dto;

import java.util.UUID;


public class UserDTO {
    private UUID id;
    private String username;
    private String avatarUrl;
    
    public UserDTO(UUID id, String username, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
}