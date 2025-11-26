package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateServerRequest;
import com.example.chatapp.dto.ServerDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Servers", description = "Server management and membership endpoints")
@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {
    
    private final ServerService serverService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<ServerDTO> createServer(
            @RequestBody CreateServerRequest request,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        ServerDTO server = serverService.createServer(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(server);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<ServerDTO>> getUserServers(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        List<ServerDTO> servers = serverService.getUserServers(userId);
        
        return ResponseEntity.ok(servers);
    }
    
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerDTO> getServerById(
            @PathVariable UUID serverId) {
        
        ServerDTO server = serverService.getServerById(serverId);
        return ResponseEntity.ok(server);
    }
    
    @PostMapping("/{serverId}/join")
    public ResponseEntity<Void> joinServer(
            @PathVariable UUID serverId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        serverService.joinServer(serverId, userId);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{serverId}/leave")
    public ResponseEntity<Void> leaveServer(
            @PathVariable UUID serverId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        serverService.leaveServer(serverId, userId);
        
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
