package com.example.chatapp.service.impl;

import com.example.chatapp.dto.ChannelDTO;
import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.pattern.factory.ChannelFactoryProvider;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.ServerRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation that uses Factory Pattern for channel creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {
    
    private final ChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final ChannelFactoryProvider channelFactory;
    
    @Override
    @Transactional
    public ChannelDTO createChannel(CreateChannelRequest request, UUID userId) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        // Validate server
        Server server = serverRepository.findById(request.serverId())
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        // Check if user is server owner
        if (!server.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Only server owner can create channels");
        }
        
        // Use Factory Pattern to create channel
        Channel channel = channelFactory.createChannel(
            request.type(),
            request.name(),
            server,
            request.settings()
        );
        
        channel = channelRepository.save(channel);
        log.info("Channel created: {} in server: {}", channel.getId(), server.getId());
        
        return convertToDTO(channel);
    }
    
    @Override
    public List<ChannelDTO> getServerChannels(UUID serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        List<Channel> channels = channelRepository.findByServerOrderByName(server);
        return channels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ChannelDTO getChannelById(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BadRequestException("Channel not found"));
        
        return convertToDTO(channel);
    }
    
    private ChannelDTO convertToDTO(Channel channel) {
        return new ChannelDTO(
            channel.getId(),
            channel.getServer() != null ? channel.getServer().getId() : null,
            channel.getName(),
            channel.getType(),
            channel.getSettings()
        );
    }
}
