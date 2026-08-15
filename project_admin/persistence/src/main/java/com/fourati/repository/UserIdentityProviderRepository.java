package com.fourati.repository;

import com.fourati.domain.UserIdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityProviderRepository extends JpaRepository<UserIdentityProvider, UUID> {

    Optional<UserIdentityProvider> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<UserIdentityProvider> findByUserIdAndProvider(UUID userId, String provider);

    List<UserIdentityProvider> findByUserId(UUID userId);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
