package com.socialchat.mapper;

import com.socialchat.dto.UserDto;
import com.socialchat.entity.User;

public class UserMapper {
    public static UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
