package com.example.chatapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;


    @OneToMany(mappedBy = "room")
    private Set<Message> messages;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true) // db
    private Set<Membership> memberships = new HashSet<>();



   
    public void addMember(User user, Membership.MemberRole role) {
        Membership membership = new Membership(user, this, role);
        memberships.add(membership);
        // user.getMemberships().add(membership);
    }

    public void removeMember(User user) {
        memberships.removeIf(membership -> membership.getUser().equals(user));
    }
}