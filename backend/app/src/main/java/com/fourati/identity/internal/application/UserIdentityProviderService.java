package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.domain.UserIdentityProvider;
import com.fourati.identity.CreateUserIdentityProviderRequest;
import com.fourati.identity.UserIdentityProviderResponse;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service linking users to external identity providers (SSO / OAuth2
 * accounts). Only link/unlink operations are exposed; there is no update of
 * an existing link beyond removing and re-linking.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserIdentityProviderService {

    private final UserIdentityProviderRepository userIdentityProviderRepository;
    private final UserIdentityProviderMapper userIdentityProviderMapper;

    @Audited(action = "CREATE", description = "Linked a user to an external identity provider")
    public UserIdentityProviderResponse create(CreateUserIdentityProviderRequest request) {
        if (userIdentityProviderRepository.existsByProviderAndProviderUserId(
                request.provider(), request.providerUserId())) {
            throw new ConflictException("This provider account is already linked to a user");
        }
        if (userIdentityProviderRepository.findByUserIdAndProvider(request.userId(), request.provider()).isPresent()) {
            throw new ConflictException("User already has a linked account for this provider");
        }
        UserIdentityProvider entity = userIdentityProviderMapper.toEntity(request);
        User user = new User();
        user.setId(request.userId());
        entity.setUser(user);
        entity.setLinkedAt(Instant.now());
        UserIdentityProvider saved = userIdentityProviderRepository.save(entity);
        return userIdentityProviderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserIdentityProviderResponse findById(UUID id) {
        return userIdentityProviderMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UserIdentityProviderResponse> findByUserId(UUID userId) {
        return userIdentityProviderRepository.findByUserId(userId).stream()
                .map(userIdentityProviderMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Unlinked a user's external identity provider")
    public void delete(UUID id) {
        UserIdentityProvider entity = getEntityOrThrow(id);
        userIdentityProviderRepository.delete(entity);
    }

    private UserIdentityProvider getEntityOrThrow(UUID id) {
        return userIdentityProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserIdentityProvider", id));
    }
}
