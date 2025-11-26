package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.enums.ChannelType;
import org.springframework.stereotype.Component;

/**
 * Factory for creating announcement channels
 */
@Component
public class AnnouncementChannelFactory implements ChannelFactory {
    
    @Override
    public Channel createChannel(String name, Server server, String settings) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setServer(server);
        channel.setType(ChannelType.ANNOUNCEMENT);
        
        // Announcement channels are read-only for most users
        String defaultSettings = settings != null ? settings : 
            "{\"readOnly\": true, \"adminOnly\": true}";
        channel.setSettings(defaultSettings);
        
        return channel;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.ANNOUNCEMENT;
    }
}
