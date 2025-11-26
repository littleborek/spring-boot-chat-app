package com.example.chatapp.controller;

import com.example.chatapp.dto.ChannelDTO;
import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.ChannelService;
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

@Tag(name = "Channels", description = "Channel management endpoints (uses Factory Pattern)")
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {
    
    private final ChannelService channelService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Operation(summary = "Create channel", description = "Create a new channel in a server (Factory Pattern)", 
               security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ChannelDTO> createChannel(
            @RequestBody CreateChannelRequest request,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        ChannelDTO channel = channelService.createChannel(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }
    
    @GetMapping("/server/{serverId}")
    public ResponseEntity<List<ChannelDTO>> getServerChannels(
            @PathVariable UUID serverId) {
        
        List<ChannelDTO> channels = channelService.getServerChannels(serverId);
        return ResponseEntity.ok(channels);
    }
    
    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelDTO> getChannelById(
            @PathVariable UUID channelId) {
        
        ChannelDTO channel = channelService.getChannelById(channelId);
        return ResponseEntity.ok(channel);
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
