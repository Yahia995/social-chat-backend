package com.socialchat.dto;

import com.socialchat.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderProfilePhotoUrl;
    private String type; // FRIEND_REQUEST, FRIEND_REQUEST_ACCEPTED, POST_LIKE, POST_COMMENT, MESSAGE, MENTION
    private String title;
    private String message;
    private Long relatedPostId;
    private Long relatedRequestId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
