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
public class ConversationDto {
    private Long id;
    private Long participantId;
    private String participantUsername;
    private String participantPhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean unread;
}
