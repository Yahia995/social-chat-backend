package com.socialchat.mapper;

import com.socialchat.dto.NotificationDto;
import com.socialchat.entity.Notification;

public class NotificationMapper {
    public static NotificationDto convertToDto(Notification notification) {
        String senderUsername = notification.getSender() != null ? notification.getSender().getUsername() : null;
        String senderDisplayName = notification.getSender() != null ? notification.getSender().getDisplayName() : null;
        String senderProfilePhotoUrl = notification.getSender() != null ? notification.getSender().getProfilePhotoUrl() : null;

        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .senderId(notification.getSender() != null ? notification.getSender().getId() : null)
                .senderUsername(senderUsername)
                .senderDisplayName(senderDisplayName)
                .senderProfilePhotoUrl(senderProfilePhotoUrl)
                .type(notification.getType().toString())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedPostId(notification.getRelatedPost() != null ? notification.getRelatedPost().getId() : null)
                .relatedRequestId(notification.getRelatedRequest() != null ? notification.getRelatedRequest().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
