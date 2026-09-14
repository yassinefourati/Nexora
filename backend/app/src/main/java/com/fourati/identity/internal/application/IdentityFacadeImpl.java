package com.fourati.identity.internal.application;

import com.fourati.identity.ApiKeyPrincipal;
import com.fourati.identity.IdentityChanged;
import com.fourati.identity.IdentityFacade;
import com.fourati.identity.ProvisionedUser;
import com.fourati.identity.RevokedCredentials;
import com.fourati.identity.UserSummary;
import com.fourati.identity.internal.domain.ApiKey;
import com.fourati.identity.internal.domain.Session;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.ApiKeyRepository;
import com.fourati.identity.internal.persistence.SessionRepository;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The identity module's public face; every method delegates to internal services and repositories. */
@Component
@RequiredArgsConstructor
@Transactional
class IdentityFacadeImpl implements IdentityFacade {

    private final UserProvisioningService userProvisioningService;
    private final ApiKeyAuthenticationService apiKeyAuthenticationService;
    private final UserRepository userRepository;
    private final UserIdentityProviderRepository identityProviderRepository;
    private final SessionRepository sessionRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ProvisionedUser provision(Jwt jwt) {
        return userProvisioningService.ensureUser(jwt);
    }

    @Override
    public Optional<ApiKeyPrincipal> authenticateApiKey(String rawKey) {
        return apiKeyAuthenticationService.authenticate(rawKey);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID userId) {
        return userId != null && userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSummary> find(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId)
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getStatus()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ownsSession(UUID sessionId, UUID userId) {
        return sessionId != null && userId != null && sessionRepository.existsByIdAndUserId(sessionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> resolveActor(UUID userId, String subject, String username) {
        if (userId != null && userRepository.existsById(userId)) {
            return Optional.of(userId);
        }
        if (subject != null) {
            Optional<UUID> bySubject = identityProviderRepository
                    .findByProviderAndProviderUserId(UserProvisioningService.PROVIDER, subject)
                    .map(link -> link.getUser().getId());
            if (bySubject.isPresent()) {
                return bySubject;
            }
        }
        if (username != null) {
            return userRepository.findByUsername(username).map(User::getId);
        }
        return Optional.empty();
    }

    @Override
    public RevokedCredentials revokeCredentialsAndSuspend(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Instant now = Instant.now();

        List<Session> sessions = sessionRepository.findByUserIdAndRevokedAtIsNull(userId);
        sessions.forEach(s -> s.revoke(now));

        List<ApiKey> keys = apiKeyRepository.findByUserIdAndRevokedAtIsNull(userId);
        keys.forEach(k -> k.revoke(now));

        user.suspend();
        eventPublisher.publishEvent(new IdentityChanged(userId, "SUSPENDED"));
        return new RevokedCredentials(sessions.size(), keys.size(), user.getStatus().value());
    }
}
