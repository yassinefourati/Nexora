package com.fourati.service;

import com.fourati.domain.ApiKey;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateApiKeyRequest;
import com.fourati.dto.request.UpdateApiKeyRequest;
import com.fourati.dto.response.ApiKeyCreatedResponse;
import com.fourati.dto.response.ApiKeyResponse;
import com.fourati.mapper.ApiKeyMapper;
import com.fourati.repository.ApiKeyRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Service for programmatic API keys issued to users. Keys are revoked rather
 * than deleted so revocation history is preserved.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;

    private static final String KEY_PREFIX = "fourati";

    /**
     * Generates the key secret server-side and returns it exactly once — only its
     * SHA-256 hash is persisted as {@code key_hash}. The caller must copy
     * {@code secret} now; it cannot be retrieved again.
     */
    @Audited(action = "CREATE", description = "Created a new API key")
    public ApiKeyCreatedResponse create(CreateApiKeyRequest request) {
        String secret = HashUtils.generateApiKey(KEY_PREFIX);
        String keyHash = HashUtils.sha256(secret);

        ApiKey apiKey = apiKeyMapper.toEntity(request);
        apiKey.setKeyHash(keyHash);
        if (request.scopes() != null) {
            apiKey.setScopes(new ArrayList<>(request.scopes()));
        }
        User user = new User();
        user.setId(request.userId());
        apiKey.setUser(user);
        ApiKey saved = apiKeyRepository.save(apiKey);
        return apiKeyMapper.toCreatedResponse(saved, secret);
    }

    @Transactional(readOnly = true)
    public ApiKeyResponse findById(UUID id) {
        return apiKeyMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponse> findAll(Pageable pageable) {
        return apiKeyRepository.findAll(pageable).map(apiKeyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponse> findByUserId(UUID userId, Pageable pageable) {
        return apiKeyRepository.findByUserId(userId, pageable).map(apiKeyMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Updated an API key")
    public ApiKeyResponse update(UUID id, UpdateApiKeyRequest request) {
        ApiKey entity = getEntityOrThrow(id);
        apiKeyMapper.updateEntityFromRequest(request, entity);
        ApiKey saved = apiKeyRepository.save(entity);
        return apiKeyMapper.toResponse(saved);
    }

    @Audited(action = "UPDATE", description = "Revoked an API key")
    public ApiKeyResponse revoke(UUID id) {
        ApiKey entity = getEntityOrThrow(id);
        entity.setRevokedAt(Instant.now());
        ApiKey saved = apiKeyRepository.save(entity);
        return apiKeyMapper.toResponse(saved);
    }

    private ApiKey getEntityOrThrow(UUID id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", id));
    }
}
