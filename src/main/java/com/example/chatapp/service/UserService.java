
package com.example.chatapp.service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.chatapp.entity.User;


public interface UserService {

    User register(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    
    Optional<User> findById(UUID id);

    List<User> findAllUsers();
    
    User updateAvatar(UUID userId, String avatarUrl);
    
    User updateProfile(UUID userId, String username, String avatarUrl, String profileMeta);

   
}