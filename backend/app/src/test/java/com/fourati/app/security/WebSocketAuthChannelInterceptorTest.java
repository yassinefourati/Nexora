package com.fourati.app.security;

import com.fourati.identity.IdentityFacade;

import com.fourati.identity.ProvisionedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The STOMP CONNECT frame is the only place WebSocket clients authenticate.
 * These tests pin: Bearer token required, invalid token rejected, principal
 * name is the backend username (what /user/queue routing keys on) resolved
 * through the same provisioning path as HTTP, and a provisioning failure is
 * refused rather than accepted anonymously.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketAuthChannelInterceptorTest {

    @Mock private JwtDecoder jwtDecoder;
    @Mock private IdentityFacade identity;
    @Mock private MessageChannel channel;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    private static Message<byte[]> connectFrame(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Jwt jwt(String sub, String username) {
        return Jwt.withTokenValue("t").header("alg", "none")
                .subject(sub).claim("preferred_username", username)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void connectWithoutAuthorization_isRejected() {
        assertThatThrownBy(() -> interceptor.preSend(connectFrame(null), channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Missing Authorization");
    }

    @Test
    void connectWithNonBearerScheme_isRejected() {
        assertThatThrownBy(() -> interceptor.preSend(connectFrame("Basic abc"), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void connectWithInvalidJwt_isRejected() {
        when(jwtDecoder.decode("bad")).thenThrow(new JwtException("expired"));

        assertThatThrownBy(() -> interceptor.preSend(connectFrame("Bearer bad"), channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Invalid or expired");
        verify(identity, never()).provision(any());
    }

    @Test
    void validJwt_setsPrincipalToTheBackendUsername_notTheClaim() {
        Jwt jwt = jwt("sub-1", "alice-kc");
        when(jwtDecoder.decode("good")).thenReturn(jwt);
        when(identity.provision(jwt)).thenReturn(new ProvisionedUser(UUID.randomUUID(), "alice"));

        Message<?> out = interceptor.preSend(connectFrame("Bearer good"), channel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(out);
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("alice");
    }

    @Test
    void provisioningFailure_isRejected() {
        Jwt jwt = jwt("sub-9", "newbie");
        when(jwtDecoder.decode("good")).thenReturn(jwt);
        when(identity.provision(jwt)).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> interceptor.preSend(connectFrame("Bearer good"), channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Unknown user");
    }

    @Test
    void nonConnectFrames_passThroughUntouched() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        Message<byte[]> frame = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThat(interceptor.preSend(frame, channel)).isSameAs(frame);
        verify(jwtDecoder, never()).decode(any());
    }
}
