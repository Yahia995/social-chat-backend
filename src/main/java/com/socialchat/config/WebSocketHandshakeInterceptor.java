package com.socialchat.config;

import com.socialchat.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = getTokenFromQueryParam(request);

            if (!StringUtils.hasText(token)) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (!StringUtils.hasText(token)) {
                log.warn("WebSocket handshake rejected: missing token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Validate token signature and expiration
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket handshake rejected: invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Check if token is revoked
            if (jwtTokenProvider.isTokenRevoked(token)) {
                log.warn("WebSocket handshake rejected: revoked token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            if (userId == null || username == null) {
                log.warn("WebSocket handshake rejected: unable to extract user info from token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", userId);
            attributes.put("username", username);
            attributes.put("token", token);

            log.debug("WebSocket handshake successful for user: {} ({})", username, userId);
            return true;

        } catch (Exception e) {
            log.error("WebSocket handshake error: {}", e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket connection error after handshake: {}", exception.getMessage());
        }
    }

    private String getTokenFromQueryParam(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query)) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }
}
