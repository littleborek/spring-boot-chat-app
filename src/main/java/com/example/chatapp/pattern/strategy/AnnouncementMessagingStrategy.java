package com.example.chatapp.pattern.strategy;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for sending server-wide announcements
 * All members of the server receive the announcement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementMessagingStrategy implements MessagingStrategy {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void sendMessage(Message message, List<User> recipients) {
        // Get server ID from channel's server
        String serverId = message.getChannel().getServer().getId().toString();
        String destination = "/topic/server/" + serverId + "/announcements";
        
        messagingTemplate.convertAndSend(destination, message);
        log.info("Announcement sent to server {} with {} recipients", 
                serverId, recipients.size());
    }
    
    @Override
    public String getStrategyType() {
        return "ANNOUNCEMENT";
    }
}
