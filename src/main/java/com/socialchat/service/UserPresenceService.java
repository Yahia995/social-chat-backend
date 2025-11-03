package com.socialchat.service;

import com.socialchat.dto.UserPresenceDto;
import com.socialchat.entity.User;
import com.socialchat.entity.UserPresence;
import com.socialchat.mapper.UserPresenceMapper;
import com.socialchat.repository.UserPresenceRepository;
import com.socialchat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserPresenceService {
    @Autowired
    private UserPresenceRepository userPresenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketEventService webSocketEventService;

    @Transactional
    public UserPresenceDto setUserOnline(Long userId, boolean online) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserPresence presence = userPresenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPresence.builder()
                        .user(user)
                        .isOnline(false)
                        .lastSeen(LocalDateTime.now())
                        .build());

        presence.setIsOnline(online);
        presence.setLastSeen(LocalDateTime.now());
        presence = userPresenceRepository.save(presence);

        UserPresenceDto dto = UserPresenceMapper.convertToDto(presence);
        
        webSocketEventService.broadcastPresence(dto);
        log.info("User {} presence set to online: {}", userId, online);
        
        return dto;
    }

    public UserPresenceDto getUserPresence(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserPresence presence = userPresenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPresence.builder()
                        .user(user)
                        .isOnline(false)
                        .lastSeen(LocalDateTime.now())
                        .build());

        return UserPresenceMapper.convertToDto(presence);
    }

    public boolean isUserOnline(Long userId) {
        Optional<UserPresence> presence = userPresenceRepository.findByUserId(userId);
        return presence.map(UserPresence::getIsOnline).orElse(false);
    }

    @Transactional
    public void updateLastSeen(Long userId) {
        userPresenceRepository.findByUserId(userId).ifPresent(presence -> {
            presence.setLastSeen(LocalDateTime.now());
            userPresenceRepository.save(presence);
        });
    }
}
