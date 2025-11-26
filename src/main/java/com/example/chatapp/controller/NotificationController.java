package com.example.chatapp.controller;

import com.example.chatapp.dto.NotificationDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications", description = "User notification endpoints (Observer Pattern)")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        
        return ResponseEntity.ok(notifications);
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId) {
        
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok().build();
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
