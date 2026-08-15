package com.fourati.config;

import com.fourati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Authenticates the STOMP CONNECT frame's Bearer token — this is what the
 * frontend's useWebSocketStore already sends via connectHeaders, since a raw
 * WebSocket/SockJS handshake can't carry a normal HTTP Authorization header
 * the way REST calls do.
 *
 * The STOMP Principal is set to the backend username (resolved via
 * preferred_username -> UserRepository.findByUsername), never the JWT sub —
 * the same identity-resolution rule as everywhere else in this app, since
 * Keycloak's subject and this backend's users.id are different UUID spaces.
 * That username is what SimpMessagingTemplate.convertAndSendToUser(...) keys
 * on for /user/queue/* destinations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new org.springframework.messaging.MessagingException("Missing Authorization header on STOMP CONNECT");
            }

            String token = authHeader.substring("Bearer ".length());
            Jwt jwt;
            try {
                jwt = jwtDecoder.decode(token);
            } catch (JwtException ex) {
                log.warn("Rejected WebSocket CONNECT: invalid JWT ({})", ex.getMessage());
                throw new org.springframework.messaging.MessagingException("Invalid or expired token");
            }

            String username = jwt.getClaimAsString("preferred_username");
            boolean known = username != null && userRepository.existsByUsername(username);
            if (!known) {
                log.warn("Rejected WebSocket CONNECT: no backend user for username={}", username);
                throw new org.springframework.messaging.MessagingException("Unknown user");
            }

            Principal principal = new StompPrincipal(username);
            accessor.setUser(principal);
        }

        return message;
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
