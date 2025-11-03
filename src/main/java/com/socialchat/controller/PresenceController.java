package com.socialchat.controller;

import com.socialchat.dto.UserPresenceDto;
import com.socialchat.service.UserPresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presence")
@Tag(name = "Presence", description = "User presence and online status endpoints")
@Slf4j
public class PresenceController {
    @Autowired
    private UserPresenceService userPresenceService;

    @PostMapping("/online")
    @Operation(summary = "Set current user as online")
    public ResponseEntity<UserPresenceDto> setOnline() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            UserPresenceDto presence = userPresenceService.setUserOnline(userId, true);
            return ResponseEntity.ok(presence);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/offline")
    @Operation(summary = "Set current user as offline")
    public ResponseEntity<UserPresenceDto> setOffline() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            UserPresenceDto presence = userPresenceService.setUserOnline(userId, false);
            return ResponseEntity.ok(presence);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's presence status")
    public ResponseEntity<UserPresenceDto> getUserPresence(@PathVariable Long userId) {
        try {
            UserPresenceDto presence = userPresenceService.getUserPresence(userId);
            return ResponseEntity.ok(presence);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}/online")
    @Operation(summary = "Check if user is online")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable Long userId) {
        try {
            boolean isOnline = userPresenceService.isUserOnline(userId);
            return ResponseEntity.ok(isOnline);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
