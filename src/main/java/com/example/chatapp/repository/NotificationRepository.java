package com.example.chatapp.repository;

import com.example.chatapp.entity.Notification;
import com.example.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByRecipientAndIsReadFalse(User recipient);
    
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
}
