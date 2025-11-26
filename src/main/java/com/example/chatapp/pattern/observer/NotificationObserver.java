package com.example.chatapp.pattern.observer;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.Notification;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.NotificationType;
import com.example.chatapp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete Observer that creates notifications when messages are created
 */
@Component
@RequiredArgsConstructor
public class NotificationObserver implements MessageObserver {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public void onMessageCreated(Message message) {
        // Create notifications for mentioned users or channel subscribers
        createNotificationsForMessage(message);
    }
    
    @Override
    public void onMessageUpdated(Message message) {
        // Optionally handle message updates
    }
    
    @Override
    public void onMessageDeleted(Long messageId) {
        // Optionally clean up notifications
    }
    
    private void createNotificationsForMessage(Message message) {
        Channel channel = message.getChannel();
        User author = message.getAuthor();
        String content = message.getContext();
        
        // Check for mentions in the message content
        if (content != null && content.contains("@")) {
            // Extract mentioned users and create notifications
            // This is a simplified version - you'd want more sophisticated mention parsing
        }
        
        // Create notification for channel members (simplified)
        Notification notification = new Notification();
        notification.setType(NotificationType.MESSAGE);
        notification.setMessage(message);
        notification.setChannel(channel);
        notification.setSender(author);
        notification.setRead(false);
        
        // Note: You'd set the recipient based on channel membership
        // This would require querying channel members
    }
}
