package com.example.chatapp.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "servers")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    private String description;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String settings;
    
    //Relationships
    
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Membership> members;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore 
    private Set<Channel> channels;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
