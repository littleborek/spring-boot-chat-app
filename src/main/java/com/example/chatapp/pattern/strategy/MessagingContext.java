package com.example.chatapp.pattern.strategy;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class that uses different messaging strategies
 * Selects appropriate strategy based on message type
 */
@Component
public class MessagingContext {
    
    private final Map<String, MessagingStrategy> strategies = new HashMap<>();
    
    public MessagingContext(List<MessagingStrategy> strategyList) {
        for (MessagingStrategy strategy : strategyList) {
            strategies.put(strategy.getStrategyType(), strategy);
        }
    }
    
    public void executeStrategy(String strategyType, Message message, List<User> recipients) {
        MessagingStrategy strategy = strategies.get(strategyType.toUpperCase());
        if (strategy == null) {
            // Default to channel strategy
            strategy = strategies.get("CHANNEL");
        }
        strategy.sendMessage(message, recipients);
    }
}
