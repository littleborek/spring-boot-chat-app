package com.example.chatapp.pattern.observer;

import com.example.chatapp.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer that broadcasts messages via WebSocket
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketObserver implements MessageObserver {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void onMessageCreated(Message message) {
        String destination = "/topic/channel/" + message.getChannel().getId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message broadcast to channel: {}", message.getChannel().getId());
    }
    
    @Override
    public void onMessageUpdated(Message message) {
        String destination = "/topic/channel/" + message.getChannel().getId() + "/updates";
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message update broadcast to channel: {}", message.getChannel().getId());
    }
    
    @Override
    public void onMessageDeleted(Long messageId) {
        // Broadcast deletion event
        log.info("Message deleted: {}", messageId);
    }
}
