package com.fourati.identity.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.identity.internal.domain.AuthLog;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.CreateAuthLogRequest;
import com.fourati.identity.AuthLogResponse;
import com.fourati.identity.internal.persistence.AuthLogRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for authentication event log entries.
 *
 * auth_logs is append-only from the API's perspective: entries are written
 * internally by the application (via {@link #record(CreateAuthLogRequest)})
 * whenever an authentication-related event occurs (login attempt, logout,
 * password reset, etc.). No public update/delete operations are exposed.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthLogService {

    private final AuthLogRepository authLogRepository;
    private final AuthLogMapper authLogMapper;

    @Audited(action = "CREATE", description = "Recorded a new auth log entry")
    public AuthLogResponse record(CreateAuthLogRequest request) {
        AuthLog authLog = authLogMapper.toEntity(request);
        if (request.userId() != null) {
            User user = new User();
            user.setId(request.userId());
            authLog.setUser(user);
        }
        AuthLog saved = authLogRepository.save(authLog);
        return authLogMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthLogResponse findById(UUID id) {
        AuthLog authLog = authLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuthLog", id));
        return authLogMapper.toResponse(authLog);
    }

    @Transactional(readOnly = true)
    public Page<AuthLogResponse> findAll(Pageable pageable) {
        return authLogRepository.findAll(pageable).map(authLogMapper::toResponse);
    }

    /** Server-side search over eventType, ipAddress, userAgent (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<AuthLogResponse> search(String q, Pageable pageable) {
        return authLogRepository.findAll(TextSearch.<AuthLog>contains(q, "eventType", "ipAddress", "userAgent"), pageable).map(authLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuthLogResponse> findByUserId(UUID userId, Pageable pageable) {
        return authLogRepository.findByUserId(userId, pageable).map(authLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuthLogResponse> findByEventType(String eventType, Pageable pageable) {
        return authLogRepository.findByEventType(eventType, pageable).map(authLogMapper::toResponse);
    }
}
