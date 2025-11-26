package com.example.chatapp.pattern.command;

import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Command to mute a user (prevent them from sending messages)
 */
@Slf4j
@RequiredArgsConstructor
public class MuteUserCommand implements Command {
    
    private final User targetUser;
    private final Membership membership;
    private final MembershipRepository membershipRepository;
    private final int durationMinutes;
    
    private boolean wasMuted;
    
    @Override
    public void execute() {
        // Store previous state
        String settings = membership.getSettings();
        wasMuted = settings != null && settings.contains("\"muted\":true");
        
        // Set mute with expiration
        LocalDateTime muteUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        String newSettings = String.format("{\"muted\":true,\"muteUntil\":\"%s\"}", muteUntil);
        membership.setSettings(newSettings);
        membershipRepository.save(membership);
        
        log.info("User {} muted for {} minutes", targetUser.getId(), durationMinutes);
    }
    
    @Override
    public void undo() {
        if (!wasMuted) {
            membership.setSettings("{\"muted\":false}");
            membershipRepository.save(membership);
            log.info("Mute removed for user {}", targetUser.getId());
        }
    }
    
    @Override
    public String getCommandName() {
        return "MUTE";
    }
}
