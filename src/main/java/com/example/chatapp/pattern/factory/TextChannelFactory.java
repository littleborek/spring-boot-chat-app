package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.enums.ChannelType;
import org.springframework.stereotype.Component;

/**
 * Factory for creating text channels
 */
@Component
public class TextChannelFactory implements ChannelFactory {
    
    @Override
    public Channel createChannel(String name, Server server, String settings) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setServer(server);
        channel.setType(ChannelType.TEXT);
        channel.setSettings(settings != null ? settings : "{}");
        return channel;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.TEXT;
    }
}
