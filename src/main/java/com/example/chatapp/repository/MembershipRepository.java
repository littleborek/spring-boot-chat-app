package com.example.chatapp.repository;

import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    
    Optional<Membership> findByUserAndServer(User user, Server server);
    
    List<Membership> findByUser(User user);
    
    List<Membership> findByServer(Server server);
    
    boolean existsByUserAndServer(User user, Server server);
    
    boolean existsByUserIdAndServerId(UUID userId, UUID serverId);
}
