package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.ApiKey;
import com.fourati.identity.internal.domain.User;
import com.fourati.platform.util.HashUtils;
import com.fourati.identity.internal.persistence.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * API keys used to be issued but never checked on a request. These tests pin
 * the lookup semantics the {@code X-Api-Key} filter relies on.
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyAuthenticationService service;

    private static ApiKey keyFor(String raw) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("alice");
        ApiKey key = new ApiKey();
        key.setId(UUID.randomUUID());
        key.setUser(user);
        key.setKeyHash(HashUtils.sha256(raw));
        key.setScopes(List.of("users.read"));
        return key;
    }

    @Test
    void validKey_resolvesUserAndScopes_andStampsLastUsed() {
        ApiKey key = keyFor("raw-secret");
        when(apiKeyRepository.findByKeyHash(HashUtils.sha256("raw-secret"))).thenReturn(Optional.of(key));

        var principal = service.authenticate("raw-secret");

        assertThat(principal).isPresent();
        assertThat(principal.get().username()).isEqualTo("alice");
        assertThat(principal.get().scopes()).containsExactly("users.read");
        assertThat(key.getLastUsedAt()).isNotNull();
    }

    @Test
    void unknownKey_isRejected() {
        when(apiKeyRepository.findByKeyHash(HashUtils.sha256("nope"))).thenReturn(Optional.empty());

        assertThat(service.authenticate("nope")).isEmpty();
    }

    @Test
    void revokedKey_isRejected() {
        ApiKey key = keyFor("raw-secret");
        key.setRevokedAt(Instant.now().minusSeconds(10));
        when(apiKeyRepository.findByKeyHash(HashUtils.sha256("raw-secret"))).thenReturn(Optional.of(key));

        assertThat(service.authenticate("raw-secret")).isEmpty();
    }

    @Test
    void expiredKey_isRejected() {
        ApiKey key = keyFor("raw-secret");
        key.setExpiresAt(Instant.now().minusSeconds(10));
        when(apiKeyRepository.findByKeyHash(HashUtils.sha256("raw-secret"))).thenReturn(Optional.of(key));

        assertThat(service.authenticate("raw-secret")).isEmpty();
    }

    @Test
    void blankKey_neverHitsTheRepository() {
        assertThat(service.authenticate("  ")).isEmpty();
        assertThat(service.authenticate(null)).isEmpty();
    }
}
