package com.fourati.identity.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.identity.internal.domain.Session;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.CreateSessionRequest;
import com.fourati.identity.UpdateSessionRequest;
import com.fourati.identity.SessionResponse;
import com.fourati.identity.internal.persistence.SessionRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for authentication sessions issued to users, identified by a hashed
 * token. Sessions are created on login and revoked (soft) rather than deleted.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Audited(action = "CREATE", description = "Issued a new session")
    public SessionResponse create(CreateSessionRequest request) {
        if (sessionRepository.existsByTokenHash(request.tokenHash())) {
            throw new ConflictException("Session already exists with the given token hash");
        }
        Session session = sessionMapper.toEntity(request);
        User user = new User();
        user.setId(request.userId());
        session.setUser(user);
        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SessionResponse findById(UUID id) {
        return sessionMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> findAll(Pageable pageable) {
        return sessionRepository.findAll(pageable).map(sessionMapper::toResponse);
    }

    /** Server-side search over ipAddress, userAgent (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<SessionResponse> search(String q, Pageable pageable) {
        return sessionRepository.findAll(TextSearch.<Session>contains(q, "ipAddress", "userAgent"), pageable).map(sessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> findByUserId(UUID userId, Pageable pageable) {
        return sessionRepository.findByUserId(userId, pageable).map(sessionMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Revoked a session")
    public SessionResponse update(UUID id, UpdateSessionRequest request) {
        Session entity = getEntityOrThrow(id);
        VersionCheck.check(Session.class, id, entity.getVersion(), request.version());
        sessionMapper.updateEntityFromRequest(request, entity);
        Session saved = sessionRepository.save(entity);
        return sessionMapper.toResponse(saved);
    }

    @Audited(action = "UPDATE", description = "Revoked a session")
    public SessionResponse revoke(UUID id) {
        Session entity = getEntityOrThrow(id);
        entity.revoke(Instant.now());
        Session saved = sessionRepository.save(entity);
        return sessionMapper.toResponse(saved);
    }

    private Session getEntityOrThrow(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
    }
}
