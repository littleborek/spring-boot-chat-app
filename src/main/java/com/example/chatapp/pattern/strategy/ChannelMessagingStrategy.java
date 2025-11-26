package com.example.chatapp.pattern.strategy;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for sending messages to a public channel
 * All members of the channel receive the message
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelMessagingStrategy implements MessagingStrategy {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void sendMessage(Message message, List<User> recipients) {
        String destination = "/topic/channel/" + message.getChannel().getId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message sent to channel {} with {} recipients", 
                message.getChannel().getId(), recipients.size());
    }
    
    @Override
    public String getStrategyType() {
        return "CHANNEL";
    }
}
