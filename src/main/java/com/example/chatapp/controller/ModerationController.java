package com.example.chatapp.controller;

import com.example.chatapp.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {
    
    private final ModerationService moderationService;
    
    @PostMapping("/kick")
    public ResponseEntity<Void> kickUser(
            @RequestParam UUID targetUserId,
            @RequestParam UUID serverId,
            @RequestParam UUID moderatorId) {
        
        moderationService.kickUser(targetUserId, serverId, moderatorId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/ban")
    public ResponseEntity<Void> banUser(
            @RequestParam UUID targetUserId,
            @RequestParam UUID serverId,
            @RequestParam UUID moderatorId) {
        
        moderationService.banUser(targetUserId, serverId, moderatorId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/mute")
    public ResponseEntity<Void> muteUser(
            @RequestParam UUID targetUserId,
            @RequestParam UUID serverId,
            @RequestParam UUID moderatorId,
            @RequestParam int durationMinutes) {
        
        moderationService.muteUser(targetUserId, serverId, moderatorId, durationMinutes);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam UUID moderatorId) {
        
        moderationService.deleteMessage(messageId, moderatorId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/join-channel")
    public ResponseEntity<Void> joinChannel(
            @RequestParam UUID userId,
            @RequestParam UUID channelId) {
        
        moderationService.joinChannel(userId, channelId);
        return ResponseEntity.ok().build();
    }
}
