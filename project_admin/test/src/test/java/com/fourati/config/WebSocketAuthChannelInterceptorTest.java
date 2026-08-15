package com.fourati.config;

import com.fourati.repository.UserRepository;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the auth gate on the STOMP CONNECT frame — this is the only thing
 * standing between "anyone who can reach /ws can subscribe as anyone" and
 * correctly-scoped per-user push notifications, so every rejection path here
 * matters as much as the happy path.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketAuthChannelInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    private final MessageChannel channel = mock(MessageChannel.class);

    private StompHeaderAccessor lastAccessor;

    private Message<byte[]> connectFrameWithAuthHeader(String authHeaderValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authHeaderValue != null) {
            accessor.setNativeHeader("Authorization", authHeaderValue);
        }
        lastAccessor = accessor;
        return org.springframework.messaging.support.MessageBuilder
                .createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Jwt jwtWithUsername(String username) {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("preferred_username", username)
                .subject("some-keycloak-subject-uuid")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void preSend_validTokenForKnownUser_setsStompPrincipalToBackendUsername() {
        Message<byte[]> message = connectFrameWithAuthHeader("Bearer valid-token");
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithUsername("superadmin"));
        when(userRepository.existsByUsername("superadmin")).thenReturn(true);

        interceptor.preSend(message, channel);

        assertThat(lastAccessor.getUser()).isNotNull();
        assertThat(lastAccessor.getUser().getName()).isEqualTo("superadmin");
    }

    @Test
    void preSend_nonConnectFrame_doesNothing_neverDecodesToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        Message<byte[]> message = org.springframework.messaging.support.MessageBuilder
                .createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, channel);

        org.mockito.Mockito.verifyNoInteractions(jwtDecoder);
    }

    @Test
    void preSend_missingAuthorizationHeader_throwsMessagingException() {
        Message<byte[]> message = connectFrameWithAuthHeader(null);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void preSend_malformedAuthorizationHeader_throwsMessagingException() {
        Message<byte[]> message = connectFrameWithAuthHeader("NotBearer sometoken");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void preSend_invalidJwt_throwsMessagingException_neverQueriesUserRepository() {
        Message<byte[]> message = connectFrameWithAuthHeader("Bearer garbage-token");
        when(jwtDecoder.decode("garbage-token")).thenThrow(new JwtException("bad signature"));

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);

        org.mockito.Mockito.verifyNoInteractions(userRepository);
    }

    @Test
    void preSend_validJwtButUnknownBackendUser_throwsMessagingException() {
        Message<byte[]> message = connectFrameWithAuthHeader("Bearer valid-token");
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithUsername("ghost-user"));
        when(userRepository.existsByUsername("ghost-user")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }
}
