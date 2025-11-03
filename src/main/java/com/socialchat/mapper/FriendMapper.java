package com.socialchat.mapper;

import com.socialchat.dto.FriendDto;
import com.socialchat.dto.FriendRequestDto;
import com.socialchat.entity.FriendRequest;
import com.socialchat.entity.User;

public class FriendMapper {
    public static FriendRequestDto convertToDto(FriendRequest friendRequest) {
        return FriendRequestDto.builder()
                .id(friendRequest.getId())
                .senderId(friendRequest.getSender().getId())
                .senderUsername(friendRequest.getSender().getUsername())
                .senderDisplayName(friendRequest.getSender().getDisplayName())
                .senderProfilePhotoUrl(friendRequest.getSender().getProfilePhotoUrl())
                .receiverId(friendRequest.getReceiver().getId())
                .receiverUsername(friendRequest.getReceiver().getUsername())
                .receiverDisplayName(friendRequest.getReceiver().getDisplayName())
                .status(friendRequest.getStatus().toString())
                .createdAt(friendRequest.getCreatedAt())
                .build();
    }

    public static FriendDto convertUserToFriendDto(User user) {
        return FriendDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }
}
