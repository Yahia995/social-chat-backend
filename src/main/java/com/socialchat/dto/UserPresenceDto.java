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
public class UserPresenceDto {
    private Long userId;
    private String username;
    private String displayName;
    private String profilePhotoUrl;
    private boolean online;
    private LocalDateTime lastSeen;
}
