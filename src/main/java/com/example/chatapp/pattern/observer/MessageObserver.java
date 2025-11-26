package com.example.chatapp.pattern.observer;

import com.example.chatapp.entity.Message;

/**
 * Observer interface for message-related events
 * Part of Observer Pattern implementation
 */
public interface MessageObserver {
    void onMessageCreated(Message message);
    void onMessageUpdated(Message message);
    void onMessageDeleted(Long messageId);
}
