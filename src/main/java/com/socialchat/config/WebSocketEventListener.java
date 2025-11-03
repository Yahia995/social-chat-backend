package com.socialchat.config;

import com.socialchat.dto.UserPresenceDto;
import com.socialchat.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserPresenceService userPresenceService;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        try {
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj != null) {
                Long userId = userIdObj instanceof Long ? (Long) userIdObj : Long.parseLong(userIdObj.toString());

                // Mark user as offline when they disconnect
                userPresenceService.setUserOnline(userId, false);
                log.info("User {} disconnected, marked as offline", userId);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect", e);
        }
    }
}
