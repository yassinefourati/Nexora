package com.fourati.service;

import com.fourati.domain.UserIdentityProvider;
import com.fourati.dto.request.CreateUserIdentityProviderRequest;
import com.fourati.mapper.UserIdentityProviderMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.repository.UserIdentityProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers UserIdentityProviderService's two distinct uniqueness rules, which
 * guard against two different real-world problems: one external account
 * being linked to two different users (account takeover risk), and one user
 * ending up with two links to the same provider (ambiguous SSO login target).
 */
@ExtendWith(MockitoExtension.class)
class UserIdentityProviderServiceTest {

    @Mock
    private UserIdentityProviderRepository userIdentityProviderRepository;

    @Mock
    private UserIdentityProviderMapper userIdentityProviderMapper;

    @InjectMocks
    private UserIdentityProviderService userIdentityProviderService;

    private CreateUserIdentityProviderRequest request(UUID userId) {
        return new CreateUserIdentityProviderRequest(userId, "keycloak", "external-id-123", null);
    }

    @Test
    void create_providerAccountAlreadyLinkedToAnotherUser_throwsConflict() {
        UUID userId = UUID.randomUUID();
        when(userIdentityProviderRepository.existsByProviderAndProviderUserId("keycloak", "external-id-123"))
                .thenReturn(true);

        assertThatThrownBy(() -> userIdentityProviderService.create(request(userId)))
                .isInstanceOf(ConflictException.class);

        verify(userIdentityProviderRepository, never()).save(any());
    }

    @Test
    void create_userAlreadyHasLinkForThisProvider_throwsConflict() {
        UUID userId = UUID.randomUUID();
        when(userIdentityProviderRepository.existsByProviderAndProviderUserId("keycloak", "external-id-123"))
                .thenReturn(false);
        when(userIdentityProviderRepository.findByUserIdAndProvider(userId, "keycloak"))
                .thenReturn(Optional.of(new UserIdentityProvider()));

        assertThatThrownBy(() -> userIdentityProviderService.create(request(userId)))
                .isInstanceOf(ConflictException.class);

        verify(userIdentityProviderRepository, never()).save(any());
    }

    @Test
    void create_novelLink_setsUserAndLinkedAtBeforeSaving() {
        UUID userId = UUID.randomUUID();
        UserIdentityProvider mapped = new UserIdentityProvider();

        when(userIdentityProviderRepository.existsByProviderAndProviderUserId("keycloak", "external-id-123"))
                .thenReturn(false);
        when(userIdentityProviderRepository.findByUserIdAndProvider(userId, "keycloak"))
                .thenReturn(Optional.empty());
        when(userIdentityProviderMapper.toEntity(request(userId))).thenReturn(mapped);
        when(userIdentityProviderRepository.save(mapped)).thenReturn(mapped);

        userIdentityProviderService.create(request(userId));

        org.assertj.core.api.Assertions.assertThat(mapped.getUser()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(mapped.getUser().getId()).isEqualTo(userId);
        org.assertj.core.api.Assertions.assertThat(mapped.getLinkedAt()).isNotNull();
        verify(userIdentityProviderRepository).save(mapped);
    }
}
