package com.socialchat.repository;

import com.socialchat.entity.TokenRevocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRevocationRepository extends JpaRepository<TokenRevocation, Long> {
    Optional<TokenRevocation> findByToken(String token);
    
    boolean existsByToken(String token);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenRevocation t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);
}
