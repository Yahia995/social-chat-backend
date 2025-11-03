package com.socialchat.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.socialchat.entity.TokenRevocation;
import com.socialchat.repository.TokenRevocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    @Autowired
    private TokenRevocationRepository tokenRevocationRepository;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username) {
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    public String generateRefreshToken(Long userId, String username) {
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .sign(getAlgorithm());
    }

    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm()).build().verify(token);
            return decodedJWT.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            log.error("Failed to get userId from token: {}", e.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm()).build().verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.error("Failed to get username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(getAlgorithm()).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public void revokeToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm()).build().verify(token);
            LocalDateTime expiresAt = decodedJWT.getExpiresAtAsInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            TokenRevocation revocation = TokenRevocation.builder()
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();

            tokenRevocationRepository.save(revocation);
            log.info("Token revoked successfully");
        } catch (JWTVerificationException e) {
            log.error("Failed to revoke token: {}", e.getMessage());
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    public boolean isTokenRevoked(String token) {
        if (tokenRevocationRepository == null) {
            log.warn("TokenRevocationRepository is not initialized");
            return false;
        }
        try {
            return tokenRevocationRepository.existsByToken(token);
        } catch (Exception e) {
            log.error("Error checking token revocation status: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm()).build().verify(token);
            return decodedJWT.getExpiresAtAsInstant().isBefore(Instant.now());
        } catch (JWTVerificationException e) {
            log.error("Token is expired or invalid: {}", e.getMessage());
            return true;
        }
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
