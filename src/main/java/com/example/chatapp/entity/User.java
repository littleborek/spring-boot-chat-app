package com.example.chatapp.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private LocalDateTime lastActiveAt;

    @Column(columnDefinition = "jsonb")
    private String profileMeta;

    // Relastionships

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Server> ownedServers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Membership> memberships;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private Set<Message> messages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Presence presence;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    private Set<Notification> notifications;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }


    
}
