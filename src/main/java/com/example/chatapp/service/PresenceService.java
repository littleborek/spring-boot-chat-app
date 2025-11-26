package com.example.chatapp.service;

import com.example.chatapp.entity.User;

import java.util.List;
import java.util.UUID;

public interface PresenceService {
    
    void updatePresence(UUID userId, String status);
    
    List<User> getOnlineMembers(UUID serverId);
}
