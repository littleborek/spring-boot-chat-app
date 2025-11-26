package com.example.chatapp.pattern.strategy;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import com.example.chatapp.pattern.singleton.WebSocketConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for sending private direct messages
 * Only the specific recipient receives the message
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessagingStrategy implements MessagingStrategy {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketConnectionManager connectionManager;
    
    @Override
    public void sendMessage(Message message, List<User> recipients) {
        for (User recipient : recipients) {
            if (connectionManager.isUserConnected(recipient.getId())) {
                String destination = "/queue/user/" + recipient.getId();
                messagingTemplate.convertAndSend(destination, message);
                log.info("Private message sent to user: {}", recipient.getId());
            } else {
                log.warn("User {} is not connected, message queued", recipient.getId());
            }
        }
    }
    
    @Override
    public String getStrategyType() {
        return "PRIVATE";
    }
}
