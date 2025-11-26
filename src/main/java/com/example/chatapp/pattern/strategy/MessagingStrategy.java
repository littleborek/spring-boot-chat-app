package com.example.chatapp.pattern.strategy;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;

import java.util.List;

/**
 * Strategy interface for different messaging strategies
 * Part of Strategy Pattern implementation
 */
public interface MessagingStrategy {
    void sendMessage(Message message, List<User> recipients);
    String getStrategyType();
}
