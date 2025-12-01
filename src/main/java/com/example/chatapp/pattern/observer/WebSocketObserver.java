package com.example.chatapp.pattern.observer;

import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

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
        
        // Convert Entity to DTO for proper JSON serialization
        MessageDTO messageDTO = new MessageDTO(
            message.getId(),
            message.getChannel().getId(),
            new UserDTO(
                message.getAuthor().getId(),
                message.getAuthor().getUsername(),
                message.getAuthor().getAvatarUrl()
            ),
            message.getContext(),
            message.getCreatedAt(),
            message.getEditedAt(),
            message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null,
            Collections.emptyList()
        );
        
        messagingTemplate.convertAndSend(destination, messageDTO);
        log.info("Message broadcast to channel: {}", message.getChannel().getId());
    }
    
    @Override
    public void onMessageUpdated(Message message) {
        String destination = "/topic/channel/" + message.getChannel().getId() + "/updates";
        
        MessageDTO messageDTO = new MessageDTO(
            message.getId(),
            message.getChannel().getId(),
            new UserDTO(
                message.getAuthor().getId(),
                message.getAuthor().getUsername(),
                message.getAuthor().getAvatarUrl()
            ),
            message.getContext(),
            message.getCreatedAt(),
            message.getEditedAt(),
            message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null,
            Collections.emptyList()
        );
        
        messagingTemplate.convertAndSend(destination, messageDTO);
        log.info("Message update broadcast to channel: {}", message.getChannel().getId());
    }
    
    @Override
    public void onMessageDeleted(Long messageId) {
        // Broadcast deletion event
        log.info("Message deleted: {}", messageId);
    }
}
