package com.socialchat.service;

import com.socialchat.dto.NotificationDto;
import com.socialchat.dto.UserPresenceDto;
import com.socialchat.dto.TypingIndicatorDto;
import com.socialchat.dto.WebSocketMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventService {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastMessage(WebSocketMessageDto message) {
        String destination = "/topic/conversation/" + message.getConversationId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message broadcasted to {}", destination);
    }

    public void broadcastTypingIndicator(TypingIndicatorDto indicator) {
        String destination = "/topic/conversation/" + indicator.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, indicator);
    }

    public void broadcastPresence(UserPresenceDto presence) {
        String destination = "/topic/presence/" + presence.getUserId();
        messagingTemplate.convertAndSend(destination, presence);
        log.info("Presence broadcasted for user {}: online={}", presence.getUserId(), presence.isOnline());
    }

    public void broadcastNotification(NotificationDto notification) {
        String destination = "/queue/user/" + notification.getUserId() + "/notifications";
        messagingTemplate.convertAndSendToUser(notification.getUserId().toString(), "/queue/notifications", notification);
        log.info("Notification broadcasted to user {}", notification.getUserId());
    }

    public void sendNotification(Long userId, NotificationDto notification) {
        String destination = "/queue/user/" + userId + "/notifications";
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification);
        log.info("Notification sent to user {}", userId);
    }
}
