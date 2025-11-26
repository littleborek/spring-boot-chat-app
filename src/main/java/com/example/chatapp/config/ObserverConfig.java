package com.example.chatapp.config;

import com.example.chatapp.pattern.observer.MessageSubject;
import com.example.chatapp.pattern.observer.NotificationObserver;
import com.example.chatapp.pattern.observer.WebSocketObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration to register observers with the message subject
 */
@Configuration
@RequiredArgsConstructor
public class ObserverConfig {
    
    private final MessageSubject messageSubject;
    private final NotificationObserver notificationObserver;
    private final WebSocketObserver webSocketObserver;
    
    @PostConstruct
    public void init() {
        // Register observers
        messageSubject.attach(notificationObserver);
        messageSubject.attach(webSocketObserver);
    }
}
