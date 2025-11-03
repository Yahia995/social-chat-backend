package com.socialchat.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank(message = "Post text is required")
    @Size(max = 5000, message = "Post text must not exceed 5000 characters")
    private String text;

    private String imageUrl;

    private Boolean isPublic = true;
}
