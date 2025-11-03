package com.socialchat.controller;

import com.socialchat.dto.FriendDto;
import com.socialchat.dto.FriendRequestDto;
import com.socialchat.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@Tag(name = "Friends", description = "Friend request and friend list endpoints")
@Slf4j
public class FriendController {
    @Autowired
    private FriendService friendService;

    @PostMapping("/request/{receiverId}")
    @Operation(summary = "Send friend request")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(@PathVariable Long receiverId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long senderId = (Long) auth.getPrincipal();
            FriendRequestDto request = friendService.sendFriendRequest(senderId, receiverId);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/request/{requestId}/accept")
    @Operation(summary = "Accept friend request")
    public ResponseEntity<FriendRequestDto> acceptFriendRequest(@PathVariable Long requestId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            FriendRequestDto request = friendService.acceptFriendRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/request/{requestId}/reject")
    @Operation(summary = "Reject friend request")
    public ResponseEntity<FriendRequestDto> rejectFriendRequest(@PathVariable Long requestId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            FriendRequestDto request = friendService.rejectFriendRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/request/{requestId}")
    @Operation(summary = "Cancel friend request")
    public ResponseEntity<Void> cancelFriendRequest(@PathVariable Long requestId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            friendService.cancelFriendRequest(requestId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending friend requests")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            List<FriendRequestDto> requests = friendService.getPendingRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/requests/sent")
    @Operation(summary = "Get sent friend requests")
    public ResponseEntity<List<FriendRequestDto>> getSentRequests() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            List<FriendRequestDto> requests = friendService.getSentRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get friends list")
    public ResponseEntity<List<FriendDto>> getFriendsList() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            List<FriendDto> friends = friendService.getFriendsList(userId);
            return ResponseEntity.ok(friends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}/friends")
    @Operation(summary = "Get user's friends list")
    public ResponseEntity<List<FriendDto>> getUserFriendsList(@PathVariable Long userId) {
        try {
            List<FriendDto> friends = friendService.getFriendsList(userId);
            return ResponseEntity.ok(friends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/check/{userId}")
    @Operation(summary = "Check if users are friends")
    public ResponseEntity<Boolean> areFriends(@PathVariable Long userId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = (Long) auth.getPrincipal();
            boolean areFriends = friendService.areFriends(currentUserId, userId);
            return ResponseEntity.ok(areFriends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
