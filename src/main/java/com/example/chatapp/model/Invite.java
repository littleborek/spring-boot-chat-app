package com.example.chatapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID; 


@Getter
@Setter
@Entity
@Table(name = "invites")
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // invite code

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; 

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at") 
    private LocalDateTime expiresAt; 

    @Column(name = "max_uses") 
    private Integer maxUses; 

    @Column(name = "uses", nullable = false)
    private int uses = 0; 

    // Constructor
    public Invite() {
        this.code = UUID.randomUUID().toString().substring(0, 8); 
        this.createdAt = LocalDateTime.now();
    }

    public Invite(ChatRoom room, User creator, LocalDateTime expiresAt, Integer maxUses) {
        this();
        this.room = room;
        this.creator = creator;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
    }


    public void incrementUses() {
        this.uses++;
    }


    public boolean isValid() {
        boolean notExpired = (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
        boolean usesAvailable = (maxUses == null || uses < maxUses);
        return notExpired && usesAvailable;
    }


   
}