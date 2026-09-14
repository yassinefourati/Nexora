package com.fourati.identity.internal.application;

import com.fourati.identity.ApiKeyPrincipal;

import com.fourati.identity.internal.domain.ApiKey;
import com.fourati.platform.util.HashUtils;
import com.fourati.identity.internal.persistence.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves a raw {@code X-Api-Key} value to the user and scopes it grants.
 * Keys are stored only as SHA-256 hashes ({@link ApiKeyService#create}), so
 * the lookup hashes the presented value and compares hashes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationService {

    /** {@code last_used_at} is written at most this often per key to keep reads cheap. */
    static final Duration LAST_USED_WRITE_INTERVAL = Duration.ofMinutes(1);

    private final ApiKeyRepository apiKeyRepository;

    @Transactional
    public Optional<ApiKeyPrincipal> authenticate(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        Optional<ApiKey> found = apiKeyRepository.findByKeyHash(HashUtils.sha256(rawKey));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ApiKey key = found.get();
        Instant now = Instant.now();
        if (key.getRevokedAt() != null) {
            log.debug("Rejected revoked API key id={}", key.getId());
            return Optional.empty();
        }
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(now)) {
            log.debug("Rejected expired API key id={}", key.getId());
            return Optional.empty();
        }
        if (key.getLastUsedAt() == null || key.getLastUsedAt().plus(LAST_USED_WRITE_INTERVAL).isBefore(now)) {
            key.setLastUsedAt(now);
        }
        return Optional.of(new ApiKeyPrincipal(
                key.getId(),
                key.getUser().getId(),
                key.getUser().getUsername(),
                List.copyOf(key.getScopes())));
    }
}
