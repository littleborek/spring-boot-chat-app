package com.example.chatapp.repository;

import com.example.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByTimestampDesc();

    
    @Query("SELECT m FROM Message m ORDER BY m.id DESC")
    List<Message> findAllByOrderByIdDesc();
}
