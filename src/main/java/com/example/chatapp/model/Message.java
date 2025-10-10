package com.example.chatapp.model;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Data
@EqualsAndHashCode(exclude = "id")

public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;  

    @Column(name = "timestamp")
    private LocalDateTime timestamp;




    @ManyToOne
    private User user;

    // Getter & Setter
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
