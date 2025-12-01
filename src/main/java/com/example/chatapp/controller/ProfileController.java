package com.example.chatapp.controller;

import com.example.chatapp.dto.UpdateAvatarRequest;
import com.example.chatapp.dto.UpdateProfileRequest;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management endpoints")
public class ProfileController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Returns the current authenticated user's profile")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal User user) {
        User currentUser = userService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(toDTO(currentUser));
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Updates the current user's profile (username, avatar, profileMeta)")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        User updatedUser = userService.updateProfile(
                user.getId(), 
                request.username(), 
                request.avatarUrl(), 
                request.profileMeta()
        );
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    @PutMapping("/avatar")
    @Operation(summary = "Update avatar", description = "Updates the current user's avatar URL")
    public ResponseEntity<UserDTO> updateAvatar(
            @RequestBody UpdateAvatarRequest request,
            @AuthenticationPrincipal User user) {
        User updatedUser = userService.updateAvatar(user.getId(), request.avatarUrl());
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getProfileMeta(),
                user.getCreatedAt()
        );
    }
}
