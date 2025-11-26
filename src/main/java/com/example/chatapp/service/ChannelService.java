package com.example.chatapp.service;

import com.example.chatapp.dto.ChannelDTO;
import com.example.chatapp.dto.CreateChannelRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    
    ChannelDTO createChannel(CreateChannelRequest request, UUID userId);
    
    List<ChannelDTO> getServerChannels(UUID serverId);
    
    ChannelDTO getChannelById(UUID channelId);
}
