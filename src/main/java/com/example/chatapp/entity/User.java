package com.example.chatapp.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private LocalDateTime lastActiveAt;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String profileMeta;

    // Relationships

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // ownedServers JSON'da gönderilmez, sonsuz döngü kırılır
    private Set<Server> ownedServers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Membership collection JSON'da gönderilmez
    private Set<Membership> memberships;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @JsonIgnore // messages JSON'da gönderilmez
    private Set<Message> messages;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // presence JSON'da gönderilmez
    private Presence presence;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore // notifications JSON'da gönderilmez
    private Set<Notification> notifications;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
