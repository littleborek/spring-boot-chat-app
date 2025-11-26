package com.example.chatapp.service.impl;

import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Presence;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.PresenceStatus;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.pattern.singleton.WebSocketConnectionManager;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.PresenceRepository;
import com.example.chatapp.repository.ServerRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation that uses Singleton Pattern for WebSocket connection management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {
    
    private final PresenceRepository presenceRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final MembershipRepository membershipRepository;
    private final WebSocketConnectionManager connectionManager;
    
    @Override
    @Transactional
    public void updatePresence(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        Presence presence = presenceRepository.findByUser(user)
                .orElse(new Presence());
        
        presence.setUser(user);
        presence.setStatus(PresenceStatus.valueOf(status.toUpperCase()));
        presence.setLastActiveAt(LocalDateTime.now());
        
        presenceRepository.save(presence);
        
        log.info("Presence updated for user {}: {}", userId, status);
    }
    
    @Override
    public List<User> getOnlineMembers(UUID serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        List<Membership> memberships = membershipRepository.findByServer(server);
        
        // Use Singleton WebSocketConnectionManager to check online status
        return memberships.stream()
                .map(Membership::getUser)
                .filter(user -> connectionManager.isUserConnected(user.getId()))
                .collect(Collectors.toList());
    }
}
