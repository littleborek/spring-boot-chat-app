package com.example.chatapp.service;

import com.example.chatapp.dto.CreateServerRequest;
import com.example.chatapp.dto.ServerDTO;

import java.util.List;
import java.util.UUID;

public interface ServerService {
    
    ServerDTO createServer(CreateServerRequest request, UUID userId);
    
    List<ServerDTO> getUserServers(UUID userId);
    
    ServerDTO getServerById(UUID serverId);
    
    void joinServer(UUID serverId, UUID userId);
    
    void leaveServer(UUID serverId, UUID userId);
}
