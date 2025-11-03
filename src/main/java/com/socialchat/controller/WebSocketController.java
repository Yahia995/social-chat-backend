package com.socialchat.controller;

import com.socialchat.dto.NotificationDto;
import com.socialchat.dto.TypingIndicatorDto;
import com.socialchat.dto.WebSocketMessageDto;
import com.socialchat.entity.Message;
import com.socialchat.entity.User;
import com.socialchat.repository.MessageRepository;
import com.socialchat.repository.UserRepository;
import com.socialchat.service.UserPresenceService;
import com.socialchat.service.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final WebSocketEventService eventService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    
    @Autowired
    private UserPresenceService userPresenceService;

    private Long extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
        if (userIdObj == null) {
            log.error("Unauthorized: no userId in session");
            return null;
        }
        
        try {
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            } else if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse userId: {}", e.getMessage());
        }
        
        return null;
    }

    @MessageMapping("/chat/{conversationId}/message")
    public void handleMessage(
            @DestinationVariable Long conversationId,
            @Payload WebSocketMessageDto message,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.error("Unauthorized message attempt: invalid userId");
                return;
            }
            
            if (message == null || !StringUtils.hasText(message.getContent())) {
                log.warn("Invalid message: content is empty");
                return;
            }
            
            message.setType("MESSAGE");
            message.setCreatedAt(LocalDateTime.now());
            message.setSenderId(userId);
            message.setConversationId(conversationId);
            
            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            message.setSenderUsername(sender.getUsername());
            message.setSenderPhotoUrl(sender.getProfilePhotoUrl());
            
            eventService.broadcastMessage(message);
            log.info("Message sent in conversation {} by user {}", conversationId, userId);
        } catch (Exception e) {
            log.error("Error handling message", e);
        }
    }

    @MessageMapping("/chat/{conversationId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable Long conversationId,
            @Payload TypingIndicatorDto indicator,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.error("Unauthorized typing indicator: invalid userId");
                return;
            }
            
            if (indicator == null) {
                log.warn("Invalid typing indicator: null payload");
                return;
            }
            
            indicator.setUserId(userId);
            indicator.setConversationId(conversationId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            indicator.setUsername(user.getUsername());
            
            eventService.broadcastTypingIndicator(indicator);
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
    }

    @MessageMapping("/presence/online")
    public void handlePresenceOnline(
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.error("Unauthorized presence update: invalid userId");
                return;
            }
            
            userPresenceService.setUserOnline(userId, true);
            log.info("User {} is now online", userId);
        } catch (Exception e) {
            log.error("Error handling presence online", e);
        }
    }

    @MessageMapping("/presence/offline")
    public void handlePresenceOffline(
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.error("Unauthorized presence update: invalid userId");
                return;
            }
            
            userPresenceService.setUserOnline(userId, false);
            log.info("User {} is now offline", userId);
        } catch (Exception e) {
            log.error("Error handling presence offline", e);
        }
    }

    @MessageMapping("/notification/send")
    public void handleNotification(
            @Payload NotificationDto notification,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.error("Unauthorized notification: invalid userId");
                return;
            }
            
            if (notification == null || notification.getUserId() == null) {
                log.warn("Invalid notification: missing recipient");
                return;
            }
            
            // Security check: users can only send notifications to themselves or authorized recipients
            if (!notification.getUserId().equals(userId)) {
                log.warn("Unauthorized notification attempt: user {} trying to send to {}", userId, notification.getUserId());
                return;
            }
            
            eventService.sendNotification(notification.getUserId(), notification);
            log.info("Notification sent to user {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Error handling notification", e);
        }
    }
}
