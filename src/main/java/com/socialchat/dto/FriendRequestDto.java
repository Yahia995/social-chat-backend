package com.socialchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestDto {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderProfilePhotoUrl;
    private Long receiverId;
    private String receiverUsername;
    private String receiverDisplayName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
