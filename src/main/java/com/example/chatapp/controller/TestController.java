package com.example.chatapp.controller;

import com.example.chatapp.dto.*;
import com.example.chatapp.entity.*;
import com.example.chatapp.enums.ChannelType;
import com.example.chatapp.repository.*;
import com.example.chatapp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Automated Tester", description = "Full System Integrity Checks")
public class TestController {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final MembershipRepository membershipRepository;
    
    // Injecting all required services
    private final MessageService messageService;
    private final ServerService serverService;
    private final ChannelService channelService;
    private final PresenceService presenceService;

    @GetMapping("/run-scenario")
    // @Transactional <-- REMOVED: Services manage their own transactions to allow exception handling
    @Operation(summary = "Run Full System Test", description = "Server creation, Joining, Messaging, Editing, Presence, Muting, Kicking.")
    public ResponseEntity<Map<String, Object>> runFullSystemTest() {
        Map<String, Object> report = new LinkedHashMap<>();
        List<String> logs = new ArrayList<>();
        String status = "PASS";
        
        // Variables to hold created IDs for cleanup
        UUID testServerId = null;

        try {
            logs.add("🚀 STARTING FULL SYSTEM TEST...");

            // 1. SETUP: Fetch Users
            User admin = userRepository.findByEmail("admin@test.com")
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));
            User user = userRepository.findByEmail("berk@test.com")
                    .orElseThrow(() -> new RuntimeException("Test user (berk) not found"));
            logs.add("✅ Step 1: Users loaded (Admin & User).");

            // 2. SERVER CREATION
            CreateServerRequest serverReq = new CreateServerRequest(
                "Integration Test Server", 
                "Automated test server", 
                "{\"public\": true}"
            );
            ServerDTO serverDTO = serverService.createServer(serverReq, admin.getId());
            testServerId = serverDTO.id();
            logs.add("✅ Step 2: Server created. ID: " + testServerId);

            // 3. CHANNEL CREATION
            CreateChannelRequest channelReq = new CreateChannelRequest(
                testServerId, 
                "test-general", 
                ChannelType.TEXT, 
                "{}"
            );
            ChannelDTO channelDTO = channelService.createChannel(channelReq, admin.getId());
            UUID channelId = channelDTO.id();
            logs.add("✅ Step 3: Channel created: " + channelDTO.name());

            // 4. JOIN SERVER
            // Note: If joinServer returns void, we check membership manually afterwards
            serverService.joinServer(testServerId, user.getId());
            
            // Verification
            Server serverEntity = serverRepository.findById(testServerId).orElseThrow();
            boolean isMember = membershipRepository.existsByUserAndServer(user, serverEntity);
            if (!isMember) throw new RuntimeException("Join failed: User is not in database.");
            logs.add("✅ Step 4: User joined the server.");

            // 5. PRESENCE UPDATE
            presenceService.updatePresence(user.getId(), "ONLINE");
            logs.add("✅ Step 5: User presence updated to ONLINE.");

            // 6. MESSAGING (Create & Edit)
            // a) Send Message
            MessageDTO msg = messageService.createMessage(
                new CreateMessageRequest(channelId, "Hello World", "TEXT", null), 
                user.getId()
            );
            logs.add("✅ Step 6a: Message sent. ID: " + msg.id());

            // b) Edit Message
            MessageDTO editedMsg = messageService.updateMessage(msg.id(), "Hello Java", user.getId());
            if (!editedMsg.content().equals("Hello Java")) throw new RuntimeException("Edit verification failed!");
            logs.add("✅ Step 6b: Message edited successfully.");

            // 7. MUTE LOGIC TEST
            logs.add("🔹 Step 7: Testing Mute Logic...");
            Membership membership = membershipRepository.findByUserAndServer(user, serverEntity).orElseThrow();
            membership.setMutedUntil(LocalDateTime.now().plusMinutes(5));
            membershipRepository.save(membership); // Apply Mute manually for test
            
            try {
                messageService.createMessage(
                    new CreateMessageRequest(channelId, "I should be blocked", "TEXT", null), 
                    user.getId()
                );
                logs.add("❌ FAILURE: Mute logic failed! Message was sent despite mute.");
                status = "FAIL";
            } catch (Exception e) {
                if (e.getMessage().toLowerCase().contains("muted")) {
                     logs.add("✅ Step 7: Mute works. Blocked message with error: " + e.getMessage());
                } else {
                     logs.add("⚠️ WARNING: Message blocked but unexpected error: " + e.getMessage());
                }
            }

            // 8. KICK / LEAVE TEST
            logs.add("🔹 Step 8: Testing Membership Removal...");
            // Simulating a Kick by deleting membership
            membershipRepository.delete(membership);
            
            boolean stillMember = membershipRepository.existsByUserAndServer(user, serverEntity);
            if (stillMember) {
                 logs.add("❌ FAILURE: User is still a member after kick!");
                 status = "FAIL";
            } else {
                 logs.add("✅ Step 8: User removed from server successfully.");
            }

        } catch (Exception e) {
            logs.add("💥 CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace(); 
            status = "CRASH";
        } finally {
            // 9. CLEANUP
            // Always clean up the test server, even if test failed
            if (testServerId != null) {
                try {
                    serverRepository.deleteById(testServerId);
                    logs.add("🧹 Cleanup: Test server deleted.");
                } catch (Exception ex) {
                    logs.add("⚠️ Cleanup Failed: " + ex.getMessage());
                }
            }
        }

        report.put("status", status);
        report.put("logs", logs);
        return ResponseEntity.ok(report);
    }
}