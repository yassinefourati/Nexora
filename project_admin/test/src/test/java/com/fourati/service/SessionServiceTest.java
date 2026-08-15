package com.fourati.service;

import com.fourati.domain.Session;
import com.fourati.dto.request.CreateSessionRequest;
import com.fourati.mapper.SessionMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers SessionService.revoke() and the create-conflict check. revoke() is
 * reachable by non-admin users on their own sessions (SessionController's
 * @PreAuthorize allows hasRole('ADMIN') or @sessionSecurity.isOwner(...)),
 * so it's important this sets revokedAt rather than deleting the row, and
 * that a not-found id fails loudly instead of silently no-op'ing.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void create_duplicateTokenHash_throwsConflict_neverSaves() {
        CreateSessionRequest request = new CreateSessionRequest(
                UUID.randomUUID(), "hash-value", Instant.now().plusSeconds(3600), "127.0.0.1", "agent");
        when(sessionRepository.existsByTokenHash("hash-value")).thenReturn(true);

        assertThatThrownBy(() -> sessionService.create(request)).isInstanceOf(ConflictException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void revoke_setsRevokedAt_doesNotDeleteTheRow() {
        UUID id = UUID.randomUUID();
        Session entity = new Session();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(sessionRepository.save(entity)).thenReturn(entity);

        sessionService.revoke(id);

        assertThat(entity.getRevokedAt()).isNotNull();
        verify(sessionRepository).save(entity);
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void revoke_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.revoke(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
