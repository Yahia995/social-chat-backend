package com.socialchat.service;

import com.socialchat.dto.FriendDto;
import com.socialchat.dto.FriendRequestDto;
import com.socialchat.entity.FriendRequest;
import com.socialchat.entity.User;
import com.socialchat.mapper.FriendMapper;
import com.socialchat.repository.FriendRequestRepository;
import com.socialchat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendService {
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public FriendRequestDto sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        User sender = userRepository.findByIdAndIsDeletedFalse(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        User receiver = userRepository.findByIdAndIsDeletedFalse(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        // Check if request already exists
        var existingRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        if (existingRequest.isPresent()) {
            FriendRequest req = existingRequest.get();
            if (req.getStatus() == FriendRequest.FriendRequestStatus.PENDING) {
                throw new IllegalArgumentException("Friend request already sent");
            }
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequest.FriendRequestStatus.PENDING)
                .build();

        friendRequest = friendRequestRepository.save(friendRequest);
        
        notificationService.createFriendRequestNotification(receiverId, senderId, friendRequest.getId());
        
        return FriendMapper.convertToDto(friendRequest);
    }

    @Transactional
    public FriendRequestDto acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!friendRequest.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to accept this request");
        }

        if (friendRequest.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        friendRequest = friendRequestRepository.save(friendRequest);
        
        notificationService.createFriendRequestAcceptedNotification(friendRequest.getSender().getId(), userId);
        
        return FriendMapper.convertToDto(friendRequest);
    }

    @Transactional
    public FriendRequestDto rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!friendRequest.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to reject this request");
        }

        if (friendRequest.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequest.FriendRequestStatus.REJECTED);
        friendRequest = friendRequestRepository.save(friendRequest);
        return FriendMapper.convertToDto(friendRequest);
    }

    @Transactional
    public void cancelFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!friendRequest.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this request");
        }

        if (friendRequest.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequest.FriendRequestStatus.CANCELLED);
        friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequestDto> getPendingRequests(Long userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.FriendRequestStatus.PENDING)
                .stream()
                .map(FriendMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FriendRequestDto> getSentRequests(Long userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequest.FriendRequestStatus.PENDING)
                .stream()
                .map(FriendMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FriendDto> getFriendsList(Long userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get all accepted friend requests where user is sender
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequest.FriendRequestStatus.ACCEPTED);
        List<FriendDto> friends = sentRequests.stream()
                .map(req -> FriendMapper.convertUserToFriendDto(req.getReceiver()))
                .collect(Collectors.toList());

        // Get all accepted friend requests where user is receiver
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.FriendRequestStatus.ACCEPTED);
        friends.addAll(receivedRequests.stream()
                .map(req -> FriendMapper.convertUserToFriendDto(req.getSender()))
                .collect(Collectors.toList()));

        return friends;
    }

    public boolean areFriends(Long userId1, Long userId2) {
        var request1 = friendRequestRepository.findBySenderIdAndReceiverId(userId1, userId2);
        if (request1.isPresent() && request1.get().getStatus() == FriendRequest.FriendRequestStatus.ACCEPTED) {
            return true;
        }

        var request2 = friendRequestRepository.findBySenderIdAndReceiverId(userId2, userId1);
        return request2.isPresent() && request2.get().getStatus() == FriendRequest.FriendRequestStatus.ACCEPTED;
    }
}
