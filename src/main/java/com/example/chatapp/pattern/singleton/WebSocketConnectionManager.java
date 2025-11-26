package com.example.chatapp.pattern.singleton;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton pattern for managing WebSocket connections
 * Thread-safe implementation using Spring's @Component
 */
@Slf4j
@Component
public class WebSocketConnectionManager {
    
    // Thread-safe map to store active WebSocket sessions
    private final Map<UUID, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Map user ID to their session
    private final Map<UUID, UUID> userToSession = new ConcurrentHashMap<>();
    
    /**
     * Register a new WebSocket session
     */
    public void registerSession(UUID userId, WebSocketSession session) {
        UUID sessionId = UUID.randomUUID();
        activeSessions.put(sessionId, session);
        userToSession.put(userId, sessionId);
        log.info("WebSocket session registered for user: {}", userId);
    }
    
    /**
     * Remove a WebSocket session
     */
    public void removeSession(UUID userId) {
        UUID sessionId = userToSession.remove(userId);
        if (sessionId != null) {
            activeSessions.remove(sessionId);
            log.info("WebSocket session removed for user: {}", userId);
        }
    }
    
    /**
     * Get session for a specific user
     */
    public WebSocketSession getSession(UUID userId) {
        UUID sessionId = userToSession.get(userId);
        return sessionId != null ? activeSessions.get(sessionId) : null;
    }
    
    /**
     * Check if user is connected
     */
    public boolean isUserConnected(UUID userId) {
        return userToSession.containsKey(userId);
    }
    
    /**
     * Get total number of active connections
     */
    public int getActiveConnectionCount() {
        return activeSessions.size();
    }
    
    /**
     * Get all active user IDs
     */
    public java.util.Set<UUID> getActiveUserIds() {
        return userToSession.keySet();
    }
}
