package com.example.chatapp.repository;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    
    List<Channel> findByServer(Server server);
    
    List<Channel> findByServerOrderByName(Server server);
}
