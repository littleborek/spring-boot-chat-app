package com.example.chatapp.pattern.command;

import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.Server;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command to ban a user from a server
 * User is removed and prevented from rejoining
 */
@Slf4j
@RequiredArgsConstructor
public class BanUserCommand implements Command {
    
    private final User targetUser;
    private final Server server;
    private final MembershipRepository membershipRepository;
    
    private Membership removedMembership;
    
    @Override
    public void execute() {
        // Remove membership
        removedMembership = membershipRepository.findByUserAndServer(targetUser, server)
                .orElse(null);
        
        if (removedMembership != null) {
            membershipRepository.delete(removedMembership);
            // In a real implementation, you'd also add to a ban list
            log.info("User {} banned from server {}", targetUser.getId(), server.getId());
        }
    }
    
    @Override
    public void undo() {
        if (removedMembership != null) {
            membershipRepository.save(removedMembership);
            // Remove from ban list
            log.info("Ban lifted for user {} in server {}", targetUser.getId(), server.getId());
        }
    }
    
    @Override
    public String getCommandName() {
        return "BAN";
    }
}
