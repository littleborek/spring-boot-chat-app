
package com.example.chatapp.controller;

import com.example.chatapp.dto.AuthenticationRequest;
import com.example.chatapp.dto.AuthenticationResponse;
import com.example.chatapp.dto.CreateUserRequest;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User registration and login endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
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
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
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