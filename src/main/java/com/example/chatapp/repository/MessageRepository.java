package com.example.chatapp.repository;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByChannelOrderByCreatedAtDesc(Channel channel);
    
    List<Message> findByAuthor(User author);
    
    List<Message> findByChannelAndContextContainingIgnoreCase(Channel channel, String keyword);
}
