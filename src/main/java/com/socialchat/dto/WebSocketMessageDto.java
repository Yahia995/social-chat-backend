package com.socialchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderPhotoUrl;
    private String content;
    private LocalDateTime createdAt;
    private String type; // MESSAGE, TYPING, PRESENCE, NOTIFICATION
}
