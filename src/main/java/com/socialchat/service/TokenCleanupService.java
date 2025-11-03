package com.socialchat.service;

import com.socialchat.repository.TokenRevocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {
    private final TokenRevocationRepository tokenRevocationRepository;

    /**
     * Scheduled task to clean up expired tokens from the database.
     * Runs every 6 hours to prevent the token_revocations table from growing unbounded.
     *
     * Why this is needed:
     * - When a user logs out, their token is added to the revocation list
     * - The revocation record has an expiresAt timestamp (same as token expiration)
     * - Without cleanup, expired revocation records accumulate forever
     * - This causes database bloat and slower queries over time
     *
     * Cleanup strategy:
     * - Since a revoked token only needs to be checked until it naturally expires,
     *   we can safely delete any revocation record where expiresAt < now
     * - Deleting expired revocations has no security impact because:
     *   1. The JWT itself is already expired and won't be accepted
     *   2. An attacker can't use an expired JWT regardless of revocation list
     */
    @Scheduled(fixedDelayString = "${token.cleanup.interval:21600000}")  // 6 hours by default (21600000 ms)
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = tokenRevocationRepository.deleteExpiredTokens(now);
            
            if (deletedCount > 0) {
                log.info("Token cleanup completed: {} expired revocation records deleted", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }
}
