package com.example.chatapp.service;

import com.example.chatapp.dto.NotificationDTO;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    List<NotificationDTO> getUserNotifications(UUID userId);
    
    void markAsRead(UUID notificationId);
    
    void markAllAsRead(UUID userId);
}
