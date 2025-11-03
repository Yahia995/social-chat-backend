package com.socialchat.controller;

import com.socialchat.dto.ConversationDto;
import com.socialchat.dto.MessageDto;
import com.socialchat.dto.PageResponse;
import com.socialchat.dto.SendMessageRequest;
import com.socialchat.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping("/with/{participantId}")
    public ResponseEntity<ConversationDto> getOrCreateConversation(
            @PathVariable Long participantId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ConversationDto conversation = conversationService.getOrCreateConversation(userId, participantId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ConversationDto>> getUserConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<ConversationDto> conversations = conversationService.getUserConversations(userId, page, size);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MessageDto message = conversationService.sendMessage(userId, conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<PageResponse<MessageDto>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<MessageDto> messages = conversationService.getConversationMessages(userId, conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        conversationService.deleteConversation(userId, conversationId);
        return ResponseEntity.noContent().build();
    }
}
