package com.fourati.identity.internal.application;

import com.fourati.identity.UserStatus;

import com.fourati.identity.IdentityFacade;

import com.fourati.identity.ProvisionedUser;

import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.domain.UserIdentityProvider;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Just-in-time provisioning of the backend {@code users} row for a Keycloak
 * identity. Keycloak owns authentication; this backend only needs a local row
 * to hang roles, notifications, sessions and audit actors on.
 *
 * Resolution order on each authenticated request (result cached per subject):
 *   1. {@code user_identity_providers(provider='keycloak', provider_user_id=sub)} — the durable link
 *   2. a {@code users} row with the same {@code preferred_username} — legacy accounts created before
 *      the link existed; the link is written so step 1 hits next time
 *   3. otherwise a new {@code users} row is created from the token's profile claims and linked
 *
 * Renaming a user in Keycloak therefore no longer orphans them: once linked by
 * {@code sub}, the username is just a display value. The returned
 * {@link ProvisionedUser#username()} is the backend username, which may differ
 * from the token claim after such a rename.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProvisioningService {

    public static final String PROVIDER = "keycloak";
    public static final String CACHE = IdentityFacade.PROVISIONED_USERS_CACHE;

    private final UserRepository userRepository;
    private final UserIdentityProviderRepository identityProviderRepository;

    /**
     * Returns the backend user for the token's subject, creating and linking
     * the row if needed. Cached by subject so steady-state requests cost no query.
     */
    @Cacheable(cacheNames = CACHE, key = "#jwt.subject")
    @Transactional
    public ProvisionedUser ensureUser(Jwt jwt) {
        String sub = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        Optional<UserIdentityProvider> link = identityProviderRepository.findByProviderAndProviderUserId(PROVIDER, sub);
        if (link.isPresent()) {
            User linked = link.get().getUser();
            return new ProvisionedUser(linked.getId(), linked.getUsername());
        }

        User user = (username == null ? Optional.<User>empty() : userRepository.findByUsername(username))
                .orElseGet(() -> createFromClaims(jwt, username, sub));

        UserIdentityProvider newLink = new UserIdentityProvider();
        newLink.setUser(user);
        newLink.setProvider(PROVIDER);
        newLink.setProviderUserId(sub);
        newLink.setLinkedAt(Instant.now());
        identityProviderRepository.save(newLink);
        log.info("Linked Keycloak subject {} to backend user {} ({})", sub, user.getId(), user.getUsername());
        return new ProvisionedUser(user.getId(), user.getUsername());
    }

    private User createFromClaims(Jwt jwt, String username, String sub) {
        User user = new User();
        user.setUsername(username != null ? username : sub);
        String email = jwt.getClaimAsString("email");
        user.setEmail(email != null ? email : user.getUsername() + "@unknown.invalid");
        user.setFirstName(jwt.getClaimAsString("given_name"));
        user.setLastName(jwt.getClaimAsString("family_name"));
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        log.info("Provisioned backend user {} for Keycloak subject {}", saved.getUsername(), sub);
        return saved;
    }
}
