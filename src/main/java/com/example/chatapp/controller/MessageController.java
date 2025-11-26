package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateMessageRequest;
import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Messages", description = "Messaging endpoints (Factory + Observer + Strategy Patterns)")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(
            @RequestBody CreateMessageRequest request,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        MessageDTO message = messageService.createMessage(request, userId);
        return ResponseEntity.ok(message);
    }
    
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageDTO> updateMessage(
            @PathVariable Long messageId,
            @RequestBody String newContent,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        MessageDTO message = messageService.updateMessage(messageId, newContent, userId);
        return ResponseEntity.ok(message);
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<MessageDTO>> getChannelMessages(@PathVariable UUID channelId) {
        List<MessageDTO> messages = messageService.getChannelMessages(channelId);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MessageDTO>> searchMessages(
            @RequestParam UUID channelId,
            @RequestParam String keyword) {
        
        List<MessageDTO> messages = messageService.searchMessages(channelId, keyword);
        return ResponseEntity.ok(messages);
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
