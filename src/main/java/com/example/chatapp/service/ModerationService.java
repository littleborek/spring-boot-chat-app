package com.example.chatapp.service;



import java.util.UUID;

public interface ModerationService {
    
    void kickUser(UUID targetUserId, UUID serverId, UUID moderatorId);
    
    void banUser(UUID targetUserId, UUID serverId, UUID moderatorId);
    
    void muteUser(UUID targetUserId, UUID serverId, UUID moderatorId, int durationMinutes);
    
    void deleteMessage(Long messageId, UUID moderatorId);
    
    void joinChannel(UUID userId, UUID channelId);
}
