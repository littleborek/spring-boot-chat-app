package com.example.chatapp.repository;

import com.example.chatapp.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {
    
    Optional<Invite> findByCode(String code);
    
    Optional<Invite> findByCodeAndIsActiveTrue(String code);
    
    List<Invite> findByServerIdAndIsActiveTrue(UUID serverId);
    
    List<Invite> findByServerId(UUID serverId);
    
    List<Invite> findByCreatedById(UUID userId);
    
    boolean existsByCode(String code);
}
