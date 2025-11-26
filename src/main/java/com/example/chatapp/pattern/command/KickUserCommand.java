package com.example.chatapp.pattern.command;

import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Command to kick a user from a server
 */
@Slf4j
@RequiredArgsConstructor
public class KickUserCommand implements Command {
    
    private final User targetUser;
    private final Server server;
    private final MembershipRepository membershipRepository;
    
    private Membership removedMembership;
    
    @Override
    public void execute() {
        // Find and remove the membership
        removedMembership = membershipRepository.findByUserAndServer(targetUser, server)
                .orElse(null);
        
        if (removedMembership != null) {
            membershipRepository.delete(removedMembership);
            log.info("User {} kicked from server {}", targetUser.getId(), server.getId());
        }
    }
    
    @Override
    public void undo() {
        if (removedMembership != null) {
            membershipRepository.save(removedMembership);
            log.info("Kick undone for user {} in server {}", targetUser.getId(), server.getId());
        }
    }
    
    @Override
    public String getCommandName() {
        return "KICK";
    }
}
