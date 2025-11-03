package com.socialchat.repository;

import com.socialchat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);
}
