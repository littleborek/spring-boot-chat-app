package com.example.chatapp.pattern.observer;

import com.example.chatapp.entity.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject (Observable) for message events
 * Manages observers and notifies them of message changes
 */
@Component
public class MessageSubject {
    
    private final List<MessageObserver> observers = new ArrayList<>();
    
    public void attach(MessageObserver observer) {
        observers.add(observer);
    }
    
    public void detach(MessageObserver observer) {
        observers.remove(observer);
    }
    
    public void notifyMessageCreated(Message message) {
        for (MessageObserver observer : observers) {
            observer.onMessageCreated(message);
        }
    }
    
    public void notifyMessageUpdated(Message message) {
        for (MessageObserver observer : observers) {
            observer.onMessageUpdated(message);
        }
    }
    
    public void notifyMessageDeleted(Long messageId) {
        for (MessageObserver observer : observers) {
            observer.onMessageDeleted(messageId);
        }
    }
}
