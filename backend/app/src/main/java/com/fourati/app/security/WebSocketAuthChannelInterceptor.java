package com.fourati.app.security;

import com.fourati.identity.IdentityFacade;

import com.fourati.identity.ProvisionedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
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
 * Authenticates the STOMP CONNECT frame's Bearer token. A raw WebSocket/SockJS
 * handshake cannot carry a normal HTTP Authorization header, so the frontend
 * sends the token in the CONNECT headers and this interceptor validates it.
 *
 * The STOMP Principal name is the backend username of the user the token maps
 * to, resolved (and provisioned when unknown) by {@link IdentityFacade}
 * exactly as the HTTP path does. That name is what
 * {@code SimpMessagingTemplate.convertAndSendToUser(...)} keys on for
 * {@code /user/queue/*} destinations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final IdentityFacade identity;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing Authorization header on STOMP CONNECT");
            }

            String token = authHeader.substring("Bearer ".length());
            Jwt jwt;
            try {
                jwt = jwtDecoder.decode(token);
            } catch (JwtException ex) {
                log.warn("Rejected WebSocket CONNECT: invalid JWT ({})", ex.getMessage());
                throw new MessagingException("Invalid or expired token");
            }

            ProvisionedUser user;
            try {
                user = identity.provision(jwt);
            } catch (RuntimeException ex) {
                log.warn("Rejected WebSocket CONNECT: no backend user for subject={} ({})", jwt.getSubject(), ex.getMessage());
                throw new MessagingException("Unknown user");
            }

            accessor.setUser(new StompPrincipal(user.username()));
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
