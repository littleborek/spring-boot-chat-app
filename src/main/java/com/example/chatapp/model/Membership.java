package com.example.chatapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "room_id"}) 
})
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room; 

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private MemberRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    
    public enum MemberRole {
        OWNER,    
        ADMIN,    
        MODERATOR,
        MEMBER   
    }


    public Membership() {
        this.joinedAt = LocalDateTime.now();
    }

    public Membership(User user, ChatRoom room, MemberRole role) {
        this.user = user;
        this.room = room;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }



}