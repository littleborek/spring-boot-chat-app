package com.example.chatapp.service.impl;

import com.example.chatapp.dto.CreateServerRequest;
import com.example.chatapp.dto.ServerDTO;
import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.MembershipRole;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.ServerRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    
    @Override
    @Transactional
    public ServerDTO createServer(CreateServerRequest request, UUID userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        Server server = new Server();
        server.setOwner(owner);
        server.setName(request.name());
        server.setDescription(request.description());
        server.setSettings(request.settings() != null ? request.settings() : "{}");
        
        server = serverRepository.save(server);
        
        // Create membership for owner
        Membership membership = new Membership();
        membership.setUser(owner);
        membership.setServer(server);
        membership.setRole(MembershipRole.OWNER);
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);
        
        log.info("Server created: {} by user: {}", server.getId(), userId);
        
        return convertToDTO(server);
    }
    
    @Override
    public List<ServerDTO> getUserServers(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        List<Membership> memberships = membershipRepository.findByUser(user);
        return memberships.stream()
                .map(m -> convertToDTO(m.getServer()))
                .collect(Collectors.toList());
    }
    
    @Override
    public ServerDTO getServerById(UUID serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        return convertToDTO(server);
    }
    
    @Override
    @Transactional
    public void joinServer(UUID serverId, UUID userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        // Check if already a member
        if (membershipRepository.existsByUserAndServer(user, server)) {
            throw new BadRequestException("User is already a member of this server");
        }
        
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setServer(server);
        membership.setRole(MembershipRole.MEMBER);
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);
        
        log.info("User {} joined server {}", userId, serverId);
    }
    
    @Override
    @Transactional
    public void leaveServer(UUID serverId, UUID userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new BadRequestException("Server not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        // Check if user is the owner
        if (server.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Server owner cannot leave the server");
        }
        
        Membership membership = membershipRepository.findByUserAndServer(user, server)
                .orElseThrow(() -> new BadRequestException("User is not a member of this server"));
        
        membershipRepository.delete(membership);
        
        log.info("User {} left server {}", userId, serverId);
    }
    
    private ServerDTO convertToDTO(Server server) {
        return new ServerDTO(
            server.getId(),
            server.getOwner().getId(),
            server.getName(),
            server.getDescription(),
            server.getCreatedAt()
        );
    }
}
