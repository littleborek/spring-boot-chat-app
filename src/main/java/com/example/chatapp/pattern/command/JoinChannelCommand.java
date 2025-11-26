package com.example.chatapp.pattern.command;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Membership;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command to join a channel
 */
@Slf4j
@RequiredArgsConstructor
public class JoinChannelCommand implements Command {
    
    private final User user;
    private final Channel channel;
    private final MembershipRepository membershipRepository;
    
    private Membership createdMembership;
    
    @Override
    public void execute() {
        // Create membership entry
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setServer(channel.getServer());
        membership.setJoinedAt(java.time.LocalDateTime.now());
        
        createdMembership = membershipRepository.save(membership);
        log.info("User {} joined channel {}", user.getId(), channel.getId());
    }
    
    @Override
    public void undo() {
        if (createdMembership != null) {
            membershipRepository.delete(createdMembership);
            log.info("User {} left channel {}", user.getId(), channel.getId());
        }
    }
    
    @Override
    public String getCommandName() {
        return "JOIN_CHANNEL";
    }
}
