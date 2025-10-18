package com.example.chatapp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Membership> memberships = new HashSet<>();


    @ManyToMany
    @JoinTable(
      name = "friendships", 
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();


    @ManyToMany(mappedBy = "friends")
    private Set<User> friendedBy = new HashSet<>();



    public Set<Membership> getMemberships() { return memberships; }
    public void setMemberships(Set<Membership> memberships) { this.memberships = memberships; }
    public Set<User> getFriends() { return friends; }
    public void setFriends(Set<User> friends) { this.friends = friends; }
    public Set<User> getFriendedBy() { return friendedBy; }
    public void setFriendedBy(Set<User> friendedBy) { this.friendedBy = friendedBy; }


    public void addFriend(User friend) {
        friends.add(friend);
        friend.getFriendedBy().add(this); 
    }

    public void removeFriend(User friend) {
        friends.remove(friend);
        friend.getFriendedBy().remove(this);
    }
    
}


