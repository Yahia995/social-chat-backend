package com.socialchat.mapper;

import com.socialchat.dto.UserPresenceDto;
import com.socialchat.entity.UserPresence;

public class UserPresenceMapper {
    public static UserPresenceDto convertToDto(UserPresence presence) {
        return UserPresenceDto.builder()
                .userId(presence.getUser().getId())
                .username(presence.getUser().getUsername())
                .displayName(presence.getUser().getDisplayName())
                .profilePhotoUrl(presence.getUser().getProfilePhotoUrl())
                .online(presence.getIsOnline())
                .lastSeen(presence.getLastSeen())
                .build();
    }
}
