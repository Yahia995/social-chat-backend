package com.socialchat.mapper;

import com.socialchat.dto.ConversationDto;
import com.socialchat.dto.MessageDto;
import com.socialchat.entity.Conversation;
import com.socialchat.entity.Message;
import com.socialchat.repository.MessageRepository;
import com.socialchat.repository.UserRepository;

public class ConversationMapper {
    public static ConversationDto mapToDto(Conversation conversation, Long currentUserId, 
                                           MessageRepository messageRepository, UserRepository userRepository) {
        Long user1Id = conversation.getUser1().getId();
        Long user2Id = conversation.getUser2().getId();
        Long participantId = user1Id.equals(currentUserId) ? user2Id : user1Id;

        var participant = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        var lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);

        return ConversationDto.builder()
                .id(conversation.getId())
                .participantId(participantId)
                .participantUsername(participant.getUsername())
                .participantPhotoUrl(participant.getProfilePhotoUrl())
                .lastMessage(lastMessage != null ? lastMessage.getText() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unread(lastMessage != null && lastMessage.getReadAt() == null && !lastMessage.getSender().getId().equals(currentUserId))
                .build();
    }

    public static MessageDto mapMessageToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderPhotoUrl(message.getSender().getProfilePhotoUrl())
                .content(message.getText())
                .read(message.getReadAt() != null)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
