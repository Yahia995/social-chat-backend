package com.socialchat.service;

import com.socialchat.dto.ConversationDto;
import com.socialchat.dto.MessageDto;
import com.socialchat.dto.PageResponse;
import com.socialchat.dto.SendMessageRequest;
import com.socialchat.entity.Conversation;
import com.socialchat.entity.Message;
import com.socialchat.entity.User;
import com.socialchat.mapper.ConversationMapper;
import com.socialchat.repository.ConversationRepository;
import com.socialchat.repository.MessageRepository;
import com.socialchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationDto getOrCreateConversation(Long currentUserId, Long participantId) {
        if (currentUserId.equals(participantId)) {
            throw new IllegalArgumentException("Cannot create conversation with yourself");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User participant = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant user not found"));

        // Check if conversation already exists
        Optional<Conversation> existingConversation = conversationRepository
                .findByParticipants(currentUserId, participantId);

        Conversation conversation = existingConversation.orElseGet(() -> {
            Conversation newConversation = new Conversation();
            newConversation.setUser1(currentUser);
            newConversation.setUser2(participant);
            newConversation.setCreatedAt(LocalDateTime.now());
            return conversationRepository.save(newConversation);
        });

        return ConversationMapper.mapToDto(conversation, currentUserId, messageRepository, userRepository);
    }

    public PageResponse<ConversationDto> getUserConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Conversation> conversations = conversationRepository.findUserConversations(userId, pageable);

        List<ConversationDto> dtos = conversations.getContent().stream()
                .map(conv -> ConversationMapper.mapToDto(conv, userId, messageRepository, userRepository))
                .toList();

        return PageResponse.<ConversationDto>builder()
                .content(dtos)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(conversations.getTotalElements())
                .totalPages(conversations.getTotalPages())
                .hasNext(conversations.hasNext())
                .hasPrevious(conversations.hasPrevious())
                .build();
    }

    public MessageDto sendMessage(Long senderId, Long conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(senderId) && !conversation.getUser2().getId().equals(senderId)) {
            throw new IllegalArgumentException("User is not part of this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message.setReadAt(null);

        Message savedMessage = messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return ConversationMapper.mapMessageToDto(savedMessage);
    }

    public PageResponse<MessageDto> getConversationMessages(Long userId, Long conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not part of this conversation");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        // Mark messages as read
        messages.getContent().stream()
                .filter(msg -> !msg.getSender().getId().equals(userId) && msg.getReadAt() == null)
                .forEach(msg -> {
                    msg.setReadAt(LocalDateTime.now());
                    messageRepository.save(msg);
                });

        List<MessageDto> dtos = messages.getContent().stream()
                .map(ConversationMapper::mapMessageToDto)
                .toList();

        return PageResponse.<MessageDto>builder()
                .content(dtos)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .hasNext(messages.hasNext())
                .hasPrevious(messages.hasPrevious())
                .build();
    }

    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not part of this conversation");
        }

        conversationRepository.delete(conversation);
    }
}
