package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.enums.ChannelType;

/**
 * Factory interface for creating different types of channels
 * Part of Factory Pattern implementation
 */
public interface ChannelFactory {
    Channel createChannel(String name, Server server, String settings);
    ChannelType getChannelType();
}
