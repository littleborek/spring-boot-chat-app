package com.example.chatapp.repository;

import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.Membership;
import com.example.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

   
    List<Membership> findByUser(User user);

  
    Optional<Membership> findByUserAndRoom(User user, ChatRoom room);

    
    List<Membership> findByRoom(ChatRoom room);

    
    List<Membership> findByRoomAndRole(ChatRoom room, Membership.MemberRole role);
}