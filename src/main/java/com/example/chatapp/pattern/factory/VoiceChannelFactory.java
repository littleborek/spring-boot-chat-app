package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.enums.ChannelType;
import org.springframework.stereotype.Component;

/**
 * Factory for creating voice channels
 */
@Component
public class VoiceChannelFactory implements ChannelFactory {
    
    @Override
    public Channel createChannel(String name, Server server, String settings) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setServer(server);
        channel.setType(ChannelType.VOICE);
        
        // Voice channels might have specific settings like bitrate, user limit
        String defaultSettings = settings != null ? settings : 
            "{\"bitrate\": 64000, \"userLimit\": 0}";
        channel.setSettings(defaultSettings);
        
        return channel;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.VOICE;
    }
}
