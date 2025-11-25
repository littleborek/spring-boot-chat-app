
package com.example.chatapp.service;
import java.util.List;
import java.util.Optional;

import com.example.chatapp.entity.User;


public interface UserService {

    User register(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllUsers();

   
}