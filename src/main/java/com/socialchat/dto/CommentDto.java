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
public class CommentDto {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorProfilePhotoUrl;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
