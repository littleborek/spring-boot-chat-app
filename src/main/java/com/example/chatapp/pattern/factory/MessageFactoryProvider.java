package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider that manages all message factories
 */
@Component
public class MessageFactoryProvider {
    
    private final Map<String, MessageFactory> factories = new HashMap<>();
    
    public MessageFactoryProvider(List<MessageFactory> factoryList) {
        for (MessageFactory factory : factoryList) {
            factories.put(factory.getMessageType(), factory);
        }
    }
    
    public Message createMessage(String type, String content, Channel channel, User author) {
        MessageFactory factory = factories.get(type.toUpperCase());
        if (factory == null) {
            // Default to text message if type not found
            factory = factories.get("TEXT");
        }
        return factory.createMessage(content, channel, author);
    }
}
