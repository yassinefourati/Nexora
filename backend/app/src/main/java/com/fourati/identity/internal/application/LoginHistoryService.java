package com.fourati.identity.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.identity.internal.domain.LoginHistory;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.LoginHistoryResponse;
import com.fourati.identity.internal.persistence.LoginHistoryRepository;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for a user's login session history.
 *
 * login_history is append-only: records are created when a user logs in, and
 * the only mutation exposed is closing an open session by recording its
 * logout timestamp.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final LoginHistoryMapper loginHistoryMapper;

    @Transactional(readOnly = true)
    public LoginHistoryResponse findById(UUID id) {
        return loginHistoryMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> findAll(Pageable pageable) {
        return loginHistoryRepository.findAll(pageable).map(loginHistoryMapper::toResponse);
    }

    /** Server-side search over ipAddress, userAgent (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> search(String q, Pageable pageable) {
        return loginHistoryRepository.findAll(TextSearch.<LoginHistory>contains(q, "ipAddress", "userAgent"), pageable).map(loginHistoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> findByUserId(UUID userId, Pageable pageable) {
        return loginHistoryRepository.findByUserId(userId, pageable).map(loginHistoryMapper::toResponse);
    }

    private LoginHistory getEntityOrThrow(UUID id) {
        return loginHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoginHistory", id));
    }
}
