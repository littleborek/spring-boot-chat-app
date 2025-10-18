package com.example.chatapp.service;

import com.example.chatapp.model.*;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.InviteRepository;
import com.example.chatapp.repository.MembershipRepository;
import org.slf4j.Logger; // Ensure imports
import org.slf4j.LoggerFactory; // Ensure imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InviteService {

    @Autowired private InviteRepository inviteRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private MembershipRepository membershipRepository;

    private static final Logger logger = LoggerFactory.getLogger(InviteService.class); // Ensure logger is defined

    /**
     * Creates a new invite code for a room.
     * @param roomId ID of the room to invite to.
     * @param creator User creating the invite.
     * @param expiresAt Expiration time (null for never).
     * @param maxUses Max number of uses (null for unlimited).
     * @return The created Invite object.
     * @throws AccessDeniedException if the creator is not the room owner.
     * @throws RuntimeException if the room is not found.
     */
    @Transactional
    public Invite createInvite(Long roomId, User creator, LocalDateTime expiresAt, Integer maxUses) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // Authorization check: Only owner can create invites (for now)
        if (room.getOwner() == null) {
             logger.error("Cannot create invite: Room {} owner is null!", roomId);
             throw new AccessDeniedException("Room owner is not set correctly.");
        }

        if (!room.getOwner().getId().equals(creator.getId())) {
             logger.warn("User {} (ID:{}) attempted to create invite for room {} owned by ID:{}",
                         creator.getUsername(), creator.getId(), roomId, room.getOwner().getId());
             throw new AccessDeniedException("Only the room owner can create invites.");
        }

        Invite invite = new Invite(room, creator, expiresAt, maxUses);
        Invite savedInvite = inviteRepository.save(invite);
        logger.info("Invite created for room {} by user {}. Code: {}", roomId, creator.getUsername(), savedInvite.getCode());
        return savedInvite;
    }

    /**
     * Allows a user to accept an invite and join a room.
     * @param inviteCode The unique invite code.
     * @param acceptingUser The user accepting the invite.
     * @return The new Membership object.
     * @throws RuntimeException if the code is invalid, expired, limit reached, or user is already a member.
     */
    @Transactional
    public Membership acceptInvite(String inviteCode, User acceptingUser) {
        Invite invite = inviteRepository.findByCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (!invite.isValid()) {
            logger.warn("Attempt to use invalid invite code: {} by user {}", inviteCode, acceptingUser.getUsername()); // Keep warning
            throw new RuntimeException("Invite code is no longer valid");
        }

        ChatRoom room = invite.getRoom();

        if (membershipRepository.findByUserAndRoom(acceptingUser, room).isPresent()) {
            logger.info("User {} already member of room {}, invite code {} not needed.", acceptingUser.getUsername(), room.getId(), inviteCode); // Info log
             throw new RuntimeException("User is already a member of this room");
        }

        // Create new membership (as MEMBER)
        Membership newMembership = new Membership(acceptingUser, room, Membership.MemberRole.MEMBER);
        Membership savedMembership = membershipRepository.save(newMembership);

        invite.incrementUses();
        inviteRepository.save(invite);

        logger.info("User {} joined room {} using invite code {}", acceptingUser.getUsername(), room.getId(), inviteCode); // Keep success log
        return savedMembership;
    }
}