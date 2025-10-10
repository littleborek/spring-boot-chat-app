package com.example.chatapp.controller;

import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        System.out.println(">>> Signup attempt: " + username);  // DEBUG
        if(userRepository.findByUsername(username) != null) {
            System.out.println(">>> Username already exists: " + username);
            return "redirect:/signup?error";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        System.out.println(">>> User saved: " + user.getId());  // DEBUG

        return "redirect:/login?signupSuccess";
    }

}
