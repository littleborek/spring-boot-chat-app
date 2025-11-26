package com.example.chatapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;


@Data
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @JsonIgnore
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    @Column(columnDefinition = "text")
    private String context;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String contentMeta;

    private LocalDateTime createdAt;
    
    private LocalDateTime editedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    @JsonIgnore
    private Message replyToMessage;

    // --- İlişkiler ---
    
    @OneToMany(mappedBy = "replyToMessage")
    @JsonIgnore
    private Set<Message> replies;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Attachment> attachments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
