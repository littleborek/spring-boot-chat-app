package com.example.chatapp.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatapp.enums.PresenceStatus;



@Data
@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @Column(name = "user_id") // Paylaşımlı Primary Key
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Bu anotasyon, 'id' alanının aynı zamanda 'user' ilişkisi için FK olduğunu belirtir
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresenceStatus status;

    private LocalDateTime lastSeen;

    @Column(columnDefinition = "jsonb")
    private String clientInfo;
}
