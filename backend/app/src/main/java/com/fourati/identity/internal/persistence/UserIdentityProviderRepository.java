package com.fourati.identity.internal.persistence;

import com.fourati.identity.internal.domain.UserIdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityProviderRepository extends JpaRepository<UserIdentityProvider, UUID>, JpaSpecificationExecutor<UserIdentityProvider> {

    /** Fetches the linked user eagerly: callers map it after the transaction ends (open-in-view is off). */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "user")
    Optional<UserIdentityProvider> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<UserIdentityProvider> findByUserIdAndProvider(UUID userId, String provider);

    List<UserIdentityProvider> findByUserId(UUID userId);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
