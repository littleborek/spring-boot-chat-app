package com.example.chatapp.controller;

import com.example.chatapp.dto.SlashCommandRequest;
import com.example.chatapp.dto.SlashCommandResponse;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.SlashCommandService;
import com.example.chatapp.service.SlashCommandService.CommandInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
@Tag(name = "Slash Commands", description = "Slash command execution endpoints")
public class SlashCommandController {

    private final SlashCommandService slashCommandService;

    @PostMapping("/execute")
    @Operation(summary = "Execute slash command", description = "Parses and executes a slash command like /kick, /ban, /mute")
    public ResponseEntity<SlashCommandResponse> executeCommand(
            @RequestBody SlashCommandRequest request,
            @AuthenticationPrincipal User user) {
        SlashCommandResponse response = slashCommandService.executeCommand(
                request.command(),
                user.getId(),
                UUID.fromString(request.serverId()),
                UUID.fromString(request.channelId())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available/{serverId}")
    @Operation(summary = "Get available commands", description = "Returns list of commands available to the user")
    public ResponseEntity<List<CommandInfo>> getAvailableCommands(
            @PathVariable UUID serverId,
            @AuthenticationPrincipal User user) {
        List<CommandInfo> commands = slashCommandService.getAvailableCommands(user.getId(), serverId);
        return ResponseEntity.ok(commands);
    }

    @GetMapping("/permission")
    @Operation(summary = "Check command permission", description = "Checks if user has permission to execute a command")
    public ResponseEntity<Boolean> checkPermission(
            @RequestParam UUID serverId,
            @RequestParam String command,
            @AuthenticationPrincipal User user) {
        boolean hasPermission = slashCommandService.hasPermission(user.getId(), serverId, command);
        return ResponseEntity.ok(hasPermission);
    }
}
