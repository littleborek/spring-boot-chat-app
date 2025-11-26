package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.enums.ChannelType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider that manages all channel factories
 * Uses Factory Pattern to create channels based on type
 */
@Component
public class ChannelFactoryProvider {
    
    private final Map<ChannelType, ChannelFactory> factories = new HashMap<>();
    
    public ChannelFactoryProvider(List<ChannelFactory> factoryList) {
        for (ChannelFactory factory : factoryList) {
            factories.put(factory.getChannelType(), factory);
        }
    }
    
    public Channel createChannel(ChannelType type, String name, Server server, String settings) {
        ChannelFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for channel type: " + type);
        }
        return factory.createChannel(name, server, settings);
    }
}
