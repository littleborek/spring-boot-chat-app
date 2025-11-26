package com.example.chatapp.repository;

import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServerRepository extends JpaRepository<Server, UUID> {
    
    List<Server> findByOwner(User owner);
}
