package com.socialchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDto {
    private Long conversationId;
    private Long userId;
    private String username;
    private boolean isTyping;
}
