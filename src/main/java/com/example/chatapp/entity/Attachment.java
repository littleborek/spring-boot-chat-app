package com.example.chatapp.entity;


import jakarta.persistence.*;
import java.util.UUID;

import lombok.Data;


@Data
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(nullable = false)
    private String storageKey; // S3 link veya key

    private String mimeType;
    
    private Integer size;
}