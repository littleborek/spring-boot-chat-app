package com.example.chatapp.controller;

import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.PresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Presence", description = "User presence and online status endpoints (Singleton Pattern)")
@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {
    
    private final PresenceService presenceService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @PutMapping("/status")
    public ResponseEntity<Void> updatePresence(
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        presenceService.updatePresence(userId, status);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/server/{serverId}/online")
    public ResponseEntity<List<UserDTO>> getOnlineMembers(
            @PathVariable UUID serverId) {
        
        List<User> onlineUsers = presenceService.getOnlineMembers(serverId);
        List<UserDTO> userDTOs = onlineUsers.stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
