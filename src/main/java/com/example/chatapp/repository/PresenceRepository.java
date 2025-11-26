package com.example.chatapp.repository;

import com.example.chatapp.entity.Presence;
import com.example.chatapp.entity.User;
import com.example.chatapp.enums.PresenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, UUID> {
    
    Optional<Presence> findByUser(User user);
    
    List<Presence> findByStatus(PresenceStatus status);
}
