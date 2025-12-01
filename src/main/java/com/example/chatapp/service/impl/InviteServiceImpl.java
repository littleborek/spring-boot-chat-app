package com.example.chatapp.service.impl;

import com.example.chatapp.dto.CreateInviteRequest;
import com.example.chatapp.dto.InviteDTO;
import com.example.chatapp.entity.Invite;
import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.MembershipRole;
import com.example.chatapp.repository.InviteRepository;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.ServerRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public InviteDTO createInvite(CreateInviteRequest request, UUID createdById) {
        Server server = serverRepository.findById(request.serverId())
                .orElseThrow(() -> new RuntimeException("Server not found"));
        
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is member of the server
        boolean isMember = membershipRepository.existsByUserIdAndServerId(createdById, request.serverId());
        if (!isMember) {
            throw new RuntimeException("You must be a member of the server to create invites");
        }
        
        String code = generateUniqueCode();
        
        LocalDateTime expiresAt = null;
        if (request.expiresInHours() != null) {
            expiresAt = LocalDateTime.now().plusHours(request.expiresInHours());
        }
        
        Invite invite = Invite.builder()
                .code(code)
                .server(server)
                .createdBy(creator)
                .expiresAt(expiresAt)
                .maxUses(request.maxUses())
                .currentUses(0)
                .isActive(true)
                .build();
        
        invite = inviteRepository.save(invite);
        
        return toDTO(invite);
    }

    @Override
    public InviteDTO getInviteByCode(String code) {
        Invite invite = inviteRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        return toDTO(invite);
    }

    @Override
    public List<InviteDTO> getServerInvites(UUID serverId) {
        return inviteRepository.findByServerId(serverId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InviteDTO> getUserInvites(UUID userId) {
        return inviteRepository.findByCreatedById(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void useInvite(String code, UUID userId) {
        Invite invite = inviteRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid or expired invite code"));
        
        if (!invite.isValid()) {
            throw new RuntimeException("This invite has expired or reached its maximum uses");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is already a member
        boolean alreadyMember = membershipRepository.existsByUserIdAndServerId(userId, invite.getServer().getId());
        if (alreadyMember) {
            throw new RuntimeException("You are already a member of this server");
        }
        
        // Add user to server
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setServer(invite.getServer());
        membership.setRole(MembershipRole.MEMBER);
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);
        
        // Increment invite uses
        invite.incrementUses();
        inviteRepository.save(invite);
    }

    @Override
    public void revokeInvite(UUID inviteId, UUID userId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        // Check if user has permission (creator or server owner)
        if (!invite.getCreatedBy().getId().equals(userId) && 
            !invite.getServer().getOwner().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to revoke this invite");
        }
        
        invite.setActive(false);
        inviteRepository.save(invite);
    }

    @Override
    public void deleteInvite(UUID inviteId, UUID userId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        // Check if user has permission (creator or server owner)
        if (!invite.getCreatedBy().getId().equals(userId) && 
            !invite.getServer().getOwner().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this invite");
        }
        
        inviteRepository.delete(invite);
    }
    
    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (inviteRepository.existsByCode(code));
        return code;
    }
    
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
    
    private InviteDTO toDTO(Invite invite) {
        return new InviteDTO(
                invite.getId(),
                invite.getCode(),
                invite.getServer().getId(),
                invite.getServer().getName(),
                invite.getCreatedBy().getId(),
                invite.getCreatedBy().getUsername(),
                invite.getCreatedAt(),
                invite.getExpiresAt(),
                invite.getMaxUses(),
                invite.getCurrentUses(),
                invite.isValid()
        );
    }
}
