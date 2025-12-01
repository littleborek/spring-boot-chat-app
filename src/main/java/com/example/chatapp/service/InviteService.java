package com.example.chatapp.service;

import com.example.chatapp.dto.CreateInviteRequest;
import com.example.chatapp.dto.InviteDTO;

import java.util.List;
import java.util.UUID;

public interface InviteService {
    
    InviteDTO createInvite(CreateInviteRequest request, UUID createdById);
    
    InviteDTO getInviteByCode(String code);
    
    List<InviteDTO> getServerInvites(UUID serverId);
    
    List<InviteDTO> getUserInvites(UUID userId);
    
    void useInvite(String code, UUID userId);
    
    void revokeInvite(UUID inviteId, UUID userId);
    
    void deleteInvite(UUID inviteId, UUID userId);
}
