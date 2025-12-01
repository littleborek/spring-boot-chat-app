package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateInviteRequest;
import com.example.chatapp.dto.InviteDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.InviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Tag(name = "Invite", description = "Server invite management endpoints")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    @Operation(summary = "Create a new invite", description = "Creates a new invite link for a server")
    public ResponseEntity<InviteDTO> createInvite(
            @RequestBody CreateInviteRequest request,
            @AuthenticationPrincipal User user) {
        InviteDTO invite = inviteService.createInvite(request, user.getId());
        return ResponseEntity.ok(invite);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get invite by code", description = "Retrieves invite details by code")
    public ResponseEntity<InviteDTO> getInviteByCode(@PathVariable String code) {
        InviteDTO invite = inviteService.getInviteByCode(code);
        return ResponseEntity.ok(invite);
    }

    @GetMapping("/server/{serverId}")
    @Operation(summary = "Get server invites", description = "Lists all invites for a server")
    public ResponseEntity<List<InviteDTO>> getServerInvites(@PathVariable UUID serverId) {
        List<InviteDTO> invites = inviteService.getServerInvites(serverId);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my invites", description = "Lists all invites created by the current user")
    public ResponseEntity<List<InviteDTO>> getMyInvites(@AuthenticationPrincipal User user) {
        List<InviteDTO> invites = inviteService.getUserInvites(user.getId());
        return ResponseEntity.ok(invites);
    }

    @PostMapping("/code/{code}/use")
    @Operation(summary = "Use invite code", description = "Uses an invite code to join a server")
    public ResponseEntity<Map<String, String>> useInvite(
            @PathVariable String code,
            @AuthenticationPrincipal User user) {
        inviteService.useInvite(code, user.getId());
        return ResponseEntity.ok(Map.of("message", "Successfully joined the server"));
    }

    @PostMapping("/{inviteId}/revoke")
    @Operation(summary = "Revoke invite", description = "Revokes an invite (makes it inactive)")
    public ResponseEntity<Map<String, String>> revokeInvite(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal User user) {
        inviteService.revokeInvite(inviteId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Invite revoked successfully"));
    }

    @DeleteMapping("/{inviteId}")
    @Operation(summary = "Delete invite", description = "Permanently deletes an invite")
    public ResponseEntity<Void> deleteInvite(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal User user) {
        inviteService.deleteInvite(inviteId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
