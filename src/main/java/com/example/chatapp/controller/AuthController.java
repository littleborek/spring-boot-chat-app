
package com.example.chatapp.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.example.chatapp.dto.AuthenticationRequest;
import com.example.chatapp.dto.AuthenticationResponse;
import com.example.chatapp.dto.CreateUserRequest;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())

            );
            System.out.println("auth valid");

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthenticationResponse(token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }

    // ✅ Kullanıcı kaydı
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody CreateUserRequest request) {
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();
        
        User saved = userService.register(user);
        
        UserDTO userDTO = new UserDTO(saved.getId(), saved.getUsername(), saved.getAvatarUrl());
        return ResponseEntity.ok(userDTO);
    }
}