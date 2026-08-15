package com.fourati.service;

import com.fourati.domain.ApiKey;
import com.fourati.dto.request.CreateApiKeyRequest;
import com.fourati.dto.response.ApiKeyCreatedResponse;
import com.fourati.dto.response.ApiKeyResponse;
import com.fourati.mapper.ApiKeyMapper;
import com.fourati.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies the ApiKeyService.create() flow: the server generates the secret
 * and its hash itself (the client-supplied keyHash field no longer exists on
 * CreateApiKeyRequest at all), only keyHash is persisted, and the raw secret
 * is exposed exactly once via ApiKeyCreatedResponse -- never via ApiKeyResponse.
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @Captor
    private ArgumentCaptor<ApiKey> apiKeyCaptor;

    private CreateApiKeyRequest newRequest() {
        return new CreateApiKeyRequest(
                "My Key",
                List.of("read", "write"),
                UUID.randomUUID(),
                Instant.now().plusSeconds(3600)
        );
    }

    /**
     * This test proves two separate create() calls (even given equivalent request
     * content) never produce the same secret or the same hash: real server-side
     * randomness.
     */
    @Test
    void create_generatesServerSideKeyMaterial_differentEachCall() {
        when(apiKeyMapper.toEntity(any(CreateApiKeyRequest.class))).thenAnswer(inv -> new ApiKey());
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));
        when(apiKeyMapper.toCreatedResponse(any(ApiKey.class), any(String.class)))
                .thenAnswer(inv -> {
                    ApiKey entity = inv.getArgument(0);
                    String secret = inv.getArgument(1);
                    return new ApiKeyCreatedResponse(
                            entity.getId(), entity.getName(), secret,
                            entity.getScopes(), null, entity.getCreatedAt(), entity.getExpiresAt());
                });

        CreateApiKeyRequest request = newRequest();

        ApiKeyCreatedResponse first = apiKeyService.create(request);
        ApiKeyCreatedResponse second = apiKeyService.create(request);

        assertThat(first.secret()).isNotBlank();
        assertThat(second.secret()).isNotBlank();
        assertThat(first.secret()).isNotEqualTo(second.secret());

        org.mockito.Mockito.verify(apiKeyRepository, org.mockito.Mockito.times(2)).save(apiKeyCaptor.capture());
        var savedEntities = apiKeyCaptor.getAllValues();
        assertThat(savedEntities).hasSize(2);
        assertThat(savedEntities.get(0).getKeyHash()).isNotNull();
        assertThat(savedEntities.get(1).getKeyHash()).isNotNull();
        assertThat(savedEntities.get(0).getKeyHash()).isNotEqualTo(savedEntities.get(1).getKeyHash());
    }

    /**
     * The persisted keyHash must be an actual hash of the secret, not the raw secret
     * itself -- i.e. create() never stores plaintext key material.
     */
    @Test
    void create_persistedKeyHashIsNotTheRawSecret() {
        when(apiKeyMapper.toEntity(any(CreateApiKeyRequest.class))).thenAnswer(inv -> new ApiKey());
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));
        when(apiKeyMapper.toCreatedResponse(any(ApiKey.class), any(String.class)))
                .thenAnswer(inv -> {
                    ApiKey entity = inv.getArgument(0);
                    String secret = inv.getArgument(1);
                    return new ApiKeyCreatedResponse(
                            entity.getId(), entity.getName(), secret,
                            entity.getScopes(), null, entity.getCreatedAt(), entity.getExpiresAt());
                });

        ApiKeyCreatedResponse response = apiKeyService.create(newRequest());

        org.mockito.Mockito.verify(apiKeyRepository).save(apiKeyCaptor.capture());
        ApiKey saved = apiKeyCaptor.getValue();

        assertThat(response.secret()).isNotBlank();
        assertThat(saved.getKeyHash()).isNotBlank();
        assertThat(saved.getKeyHash()).isNotEqualTo(response.secret());
        // Server-generated secret is prefixed "fourati_..."; keyHash is a 64-char
        // SHA-256 hex digest -- neither is the raw secret.
        assertThat(saved.getKeyHash()).hasSize(64);
        assertThat(response.secret()).startsWith("fourati_");
    }

    /**
     * Structural check: ApiKeyResponse (returned from findById/findAll/etc.) must not
     * expose the raw secret through any record component -- no component named "secret"
     * or "key".
     */
    @Test
    void apiKeyResponse_hasNoSecretExposingComponent() {
        RecordComponent[] components = ApiKeyResponse.class.getRecordComponents();
        assertThat(components).isNotNull();

        Set<String> componentNames = java.util.Arrays.stream(components)
                .map(RecordComponent::getName)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(componentNames).doesNotContain("secret");
        assertThat(componentNames).noneMatch(name -> name.contains("secret"));
        assertThat(componentNames).doesNotContain("key");
        assertThat(componentNames).doesNotContain("keyhash");
    }

    /**
     * findById/findAll only ever go through ApiKeyMapper.toResponse(...), which never
     * receives the raw secret (it isn't stored anywhere after create() returns) -- so
     * there is structurally no path for the secret to leak through ApiKeyResponse.
     */
    @Test
    void findById_usesToResponseMapper_notToCreatedResponse() {
        UUID id = UUID.randomUUID();
        ApiKey entity = new ApiKey();
        ApiKeyResponse expected = new ApiKeyResponse(id, "name", List.of("read"),
                UUID.randomUUID(), Instant.now(), null, null, null);

        when(apiKeyRepository.findById(id)).thenReturn(java.util.Optional.of(entity));
        when(apiKeyMapper.toResponse(entity)).thenReturn(expected);

        ApiKeyResponse actual = apiKeyService.findById(id);

        assertThat(actual).isEqualTo(expected);
        org.mockito.Mockito.verify(apiKeyMapper).toResponse(entity);
        org.mockito.Mockito.verify(apiKeyMapper, org.mockito.Mockito.never())
                .toCreatedResponse(any(), any());
    }
}
