package com.example.chatapp.service.impl;

import com.example.chatapp.dto.NotificationDTO;
import com.example.chatapp.entity.Notification;
import com.example.chatapp.entity.User;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.repository.NotificationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<NotificationDTO> getUserNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found"));
        
        notification.setRead(true);
        notificationRepository.save(notification);
        
        log.info("Notification {} marked as read", notificationId);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndIsReadFalse(user);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("All notifications marked as read for user {}", userId);
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getRecipient().getId(),
            notification.getSender() != null ? notification.getSender().getId() : null,
            notification.getType(),
            notification.getMessage() != null ? notification.getMessage().getId() : null,
            notification.getChannel() != null ? notification.getChannel().getId() : null,
            notification.getServer() != null ? notification.getServer().getId() : null,
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
