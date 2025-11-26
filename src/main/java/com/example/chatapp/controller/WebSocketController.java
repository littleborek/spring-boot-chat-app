package com.example.chatapp.controller;

import com.example.chatapp.pattern.singleton.WebSocketConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket controller for handling real-time chat events
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final WebSocketConnectionManager connectionManager;
    
    @MessageMapping("/chat.connect")
    @SendTo("/topic/public")
    public String connect(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        UUID userUuid = UUID.fromString(userId);
        
        // Store session in connection manager
        headerAccessor.getSessionAttributes().put("userId", userUuid);
        
        log.info("User connected: {}", userId);
        return userId + " joined the chat";
    }
    
    @MessageMapping("/chat.disconnect")
    public void disconnect(@Payload String userId) {
        UUID userUuid = UUID.fromString(userId);
        connectionManager.removeSession(userUuid);
        
        log.info("User disconnected: {}", userId);
    }
    
    @MessageMapping("/chat.typing")
    @SendTo("/topic/typing")
    public String typing(@Payload String message) {
        return message;
    }
}
