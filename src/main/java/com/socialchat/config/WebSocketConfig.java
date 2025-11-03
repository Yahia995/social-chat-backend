package com.socialchat.config;

import com.socialchat.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins("*")
                .withSockJS()
                .setInterceptors(new WebSocketHandshakeInterceptor(jwtTokenProvider));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");

                        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);

                            if (!jwtTokenProvider.validateToken(token)) {
                                log.warn("Invalid JWT token in WebSocket connection");
                                throw new AuthenticationException("Invalid token") {};
                            }

                            if (jwtTokenProvider.isTokenRevoked(token)) {
                                log.warn("Revoked JWT token attempted for WebSocket connection");
                                throw new AuthenticationException("Token revoked") {};
                            }

                            Long userId = jwtTokenProvider.getUserIdFromToken(token);
                            String username = jwtTokenProvider.getUsernameFromToken(token);

                            accessor.getSessionAttributes().put("userId", userId);
                            accessor.getSessionAttributes().put("username", username);
                            accessor.getSessionAttributes().put("token", token);

                            log.debug("WebSocket user authenticated: {} ({})", username, userId);
                        } else {
                            log.warn("WebSocket connection attempt without Authorization header");
                            throw new AuthenticationException("Missing authorization") {};
                        }
                    } catch (AuthenticationException e) {
                        log.error("WebSocket authentication failed: {}", e.getMessage());
                        throw e;
                    }
                }

                return message;
            }
        });
    }
}
