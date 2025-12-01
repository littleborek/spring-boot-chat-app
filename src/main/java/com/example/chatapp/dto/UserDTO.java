package com.example.chatapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;


public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String profileMeta;
    private LocalDateTime createdAt;
    
    // Constructor with all fields
    public UserDTO(UUID id, String username, String email, String avatarUrl, String profileMeta, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.profileMeta = profileMeta;
        this.createdAt = createdAt;
    }
    
    // Backward compatible constructor
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
    
    public String getEmail() {
        return email;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public String getProfileMeta() {
        return profileMeta;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}