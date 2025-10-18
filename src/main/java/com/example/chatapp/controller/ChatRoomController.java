package com.example.chatapp.controller;

import com.example.chatapp.dto.ChatRoomDTO;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.Invite;
import com.example.chatapp.model.Membership;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.UserRepository; 
import com.example.chatapp.service.InviteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatRoomController {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);


    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private InviteService inviteService;

    /**
     * Lists rooms the current authenticated user is a member of.
     * @return List of ChatRoomDTOs or error response.
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Basic check for authenticated user
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {

             logger.error("Authenticated user '{}' not found in database!", currentUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<Membership> memberships = membershipRepository.findByUser(currentUser);
        List<ChatRoomDTO> roomDTOs = memberships.stream()
                .map(membership -> new ChatRoomDTO(membership.getRoom().getId(), membership.getRoom().getName()))
                .collect(Collectors.toList());


        return ResponseEntity.ok(roomDTOs);
    }

    /**
     * Creates a new chat room and adds the creator as the OWNER.
     * @param name Name of the new room.
     * @return The created ChatRoomDTO or error response.
     */
    @PostMapping("/rooms")
    public ResponseEntity<?> createChatRoom(@RequestParam String name) { 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {
            logger.error("Authenticated user '{}' not found!", currentUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found.");
        }

        if (chatRoomRepository.findByName(name).isPresent()) {
            logger.warn("Attempt to create duplicate room name: {}", name);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Room name already exists.");
        }

 

        ChatRoom newRoom = new ChatRoom();
        newRoom.setName(name);
        newRoom.setOwner(currentUser);
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        // Add creator as OWNER
        Membership ownerMembership = new Membership(currentUser, savedRoom, Membership.MemberRole.OWNER);
        membershipRepository.save(ownerMembership);

        logger.info("Room '{}' (ID:{}) created by user {}", savedRoom.getName(), savedRoom.getId(), currentUsername); // Keep success log
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatRoomDTO(savedRoom.getId(), savedRoom.getName()));
    }

     /**
     * Creates an invite code for a specific room.
     * Only the room owner can perform this action (checked in InviteService).
     * @param roomId ID of the room.
     * @return The invite code (String) or error response.
     */
    @PostMapping("/rooms/{roomId}/invites")
    public ResponseEntity<?> createRoomInvite(@PathVariable Long roomId) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {
             logger.error("Authenticated user '{}' not found!", currentUsername);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found.");
        }


        try {
            // Service handles authorization and creation
            Invite invite = inviteService.createInvite(roomId, currentUser, null, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(invite.getCode());

        } catch (RuntimeException e) {
            // Log specific error (e.g., Room not found)
            logger.error("Error creating invite for room {}: {}", roomId, e.getMessage());
            HttpStatus status = (e.getMessage().contains("Room not found")) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(e.getMessage());
        }
    }

    /**
     * Allows the authenticated user to accept an invite code and join a room.
     * @param inviteCode The invite code.
     * @return The joined ChatRoomDTO or error response.
     */
    @PostMapping("/invites/{inviteCode}/accept")
    public ResponseEntity<?> acceptRoomInvite(@PathVariable String inviteCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {
             logger.error("Authenticated user '{}' not found!", currentUsername);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found.");
        }

        // logger.info("User {} accepting invite code {}", currentUsername, inviteCode); // Can be DEBUG level

        try {
            // Service handles validation and membership creation
            Membership membership = inviteService.acceptInvite(inviteCode, currentUser);
            ChatRoom joinedRoom = membership.getRoom();
            return ResponseEntity.ok(new ChatRoomDTO(joinedRoom.getId(), joinedRoom.getName()));

        } catch (RuntimeException e) {
            // Log specific error (Invalid code, Already member etc.)
            logger.warn("Failed to accept invite code '{}' for user {}: {}", inviteCode, currentUsername, e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST; // Default
            if (e.getMessage().contains("Invalid invite code") || e.getMessage().contains("no longer valid")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("already a member")) {
                status = HttpStatus.CONFLICT;
            }
            return ResponseEntity.status(status).body(e.getMessage());
        }
    }
}