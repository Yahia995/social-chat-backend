package com.socialchat.service;

import com.socialchat.dto.NotificationDto;
import com.socialchat.dto.PageResponse;
import com.socialchat.entity.*;
import com.socialchat.mapper.NotificationMapper;
import com.socialchat.mapper.PostMapper;
import com.socialchat.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private WebSocketEventService webSocketEventService;

    @Transactional
    public NotificationDto createNotification(Long userId, Notification.NotificationType type, String title, 
                                             String message, Long senderId, Long relatedPostId, Long relatedRequestId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .sender(senderId != null ? userRepository.findByIdAndIsDeletedFalse(senderId).orElse(null) : null)
                .type(type)
                .title(title)
                .message(message)
                .relatedPost(relatedPostId != null ? postRepository.findById(relatedPostId).orElse(null) : null)
                .relatedRequest(relatedRequestId != null ? friendRequestRepository.findById(relatedRequestId).orElse(null) : null)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        NotificationDto dto = NotificationMapper.convertToDto(notification);
        
        webSocketEventService.broadcastNotification(dto);
        log.info("Notification created for user {} with type {}", userId, type);
        
        return dto;
    }

    @Transactional
    public void createFriendRequestNotification(Long receiverId, Long senderId, Long requestId) {
        User sender = userRepository.findByIdAndIsDeletedFalse(senderId).orElse(null);
        String senderName = sender != null ? sender.getDisplayName() : "Unknown User";
        
        createNotification(
            receiverId,
            Notification.NotificationType.FRIEND_REQUEST,
            "Friend Request",
            senderName + " sent you a friend request",
            senderId,
            null,
            requestId
        );
    }

    @Transactional
    public void createFriendRequestAcceptedNotification(Long receiverId, Long senderId) {
        User sender = userRepository.findByIdAndIsDeletedFalse(senderId).orElse(null);
        String senderName = sender != null ? sender.getDisplayName() : "Unknown User";
        
        createNotification(
            receiverId,
            Notification.NotificationType.FRIEND_REQUEST_ACCEPTED,
            "Friend Request Accepted",
            senderName + " accepted your friend request",
            senderId,
            null,
            null
        );
    }

    @Transactional
    public void createPostLikeNotification(Long postAuthorId, Long likerId, Long postId) {
        User liker = userRepository.findByIdAndIsDeletedFalse(likerId).orElse(null);
        String likerName = liker != null ? liker.getDisplayName() : "Unknown User";
        
        createNotification(
            postAuthorId,
            Notification.NotificationType.POST_LIKE,
            "Post Liked",
            likerName + " liked your post",
            likerId,
            postId,
            null
        );
    }

    @Transactional
    public void createPostCommentNotification(Long postAuthorId, Long commenterId, Long postId) {
        User commenter = userRepository.findByIdAndIsDeletedFalse(commenterId).orElse(null);
        String commenterName = commenter != null ? commenter.getDisplayName() : "Unknown User";
        
        createNotification(
            postAuthorId,
            Notification.NotificationType.POST_COMMENT,
            "Post Comment",
            commenterName + " commented on your post",
            commenterId,
            postId,
            null
        );
    }

    public PageResponse<NotificationDto> getNotifications(Long userId, int page, int size) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<NotificationDto> notificationDtos = notifications.map(NotificationMapper::convertToDto);
        
        return PostMapper.convertPageToResponse(notificationDtos);
    }

    public PageResponse<NotificationDto> getUnreadNotifications(Long userId, int page, int size) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        Page<NotificationDto> notificationDtos = notifications.map(NotificationMapper::convertToDto);
        
        return PostMapper.convertPageToResponse(notificationDtos);
    }

    public long getUnreadNotificationCount(Long userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to mark this notification as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        
        log.info("Notification {} marked as read", notificationId);
        return NotificationMapper.convertToDto(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }
}
