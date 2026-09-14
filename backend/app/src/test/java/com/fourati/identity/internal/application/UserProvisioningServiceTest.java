package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.domain.UserIdentityProvider;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.identity.ProvisionedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserIdentityProviderRepository identityProviderRepository;

    @InjectMocks
    private UserProvisioningService service;

    private static Jwt jwt(String sub, String username) {
        return Jwt.withTokenValue("t").header("alg", "none")
                .subject(sub)
                .claim("preferred_username", username)
                .claim("email", username + "@example.com")
                .claim("given_name", "Ali").claim("family_name", "Ce")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    private static User user(String username) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        return u;
    }

    @Test
    void linkedSubject_returnsLinkedUser_withBackendUsername_withoutTouchingUsers() {
        User alice = user("alice-backend");
        UserIdentityProvider link = new UserIdentityProvider();
        link.setUser(alice);
        when(identityProviderRepository.findByProviderAndProviderUserId("keycloak", "sub-1")).thenReturn(Optional.of(link));

        ProvisionedUser result = service.ensureUser(jwt("sub-1", "alice-renamed-in-keycloak"));

        assertThat(result.id()).isEqualTo(alice.getId());
        assertThat(result.username()).isEqualTo("alice-backend");
        verify(userRepository, never()).save(any());
        verify(identityProviderRepository, never()).save(any());
    }

    @Test
    void legacyUserByUsername_getsLinked_notDuplicated() {
        User alice = user("alice");
        when(identityProviderRepository.findByProviderAndProviderUserId("keycloak", "sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        assertThat(service.ensureUser(jwt("sub-1", "alice")).id()).isEqualTo(alice.getId());

        ArgumentCaptor<UserIdentityProvider> captor = ArgumentCaptor.forClass(UserIdentityProvider.class);
        verify(identityProviderRepository).save(captor.capture());
        assertThat(captor.getValue().getProviderUserId()).isEqualTo("sub-1");
        assertThat(captor.getValue().getUser()).isSameAs(alice);
        verify(userRepository, never()).save(any());
    }

    @Test
    void unknownSubject_createsUserFromClaims_andLinks() {
        when(identityProviderRepository.findByProviderAndProviderUserId("keycloak", "sub-9")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newbie")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        ProvisionedUser result = service.ensureUser(jwt("sub-9", "newbie"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("newbie");
        assertThat(captor.getValue().getEmail()).isEqualTo("newbie@example.com");
        assertThat(result.id()).isEqualTo(captor.getValue().getId());
        assertThat(result.username()).isEqualTo("newbie");
        verify(identityProviderRepository).save(any(UserIdentityProvider.class));
    }
}
