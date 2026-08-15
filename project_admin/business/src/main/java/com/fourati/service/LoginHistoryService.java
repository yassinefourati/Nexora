package com.fourati.service;

import com.fourati.domain.LoginHistory;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateLoginHistoryRequest;
import com.fourati.dto.request.UpdateLoginHistoryRequest;
import com.fourati.dto.response.LoginHistoryResponse;
import com.fourati.mapper.LoginHistoryMapper;
import com.fourati.repository.LoginHistoryRepository;
import com.fourati.platform.audit.Audited;
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

    @Audited(action = "CREATE", description = "Recorded a new login history entry")
    public LoginHistoryResponse create(CreateLoginHistoryRequest request) {
        LoginHistory loginHistory = loginHistoryMapper.toEntity(request);
        User user = new User();
        user.setId(request.userId());
        loginHistory.setUser(user);
        if (loginHistory.getLoginAt() == null) {
            loginHistory.setLoginAt(Instant.now());
        }
        if (request.success() != null) {
            loginHistory.setSuccess(request.success());
        }
        LoginHistory saved = loginHistoryRepository.save(loginHistory);
        return loginHistoryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoginHistoryResponse findById(UUID id) {
        return loginHistoryMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> findAll(Pageable pageable) {
        return loginHistoryRepository.findAll(pageable).map(loginHistoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> findByUserId(UUID userId, Pageable pageable) {
        return loginHistoryRepository.findByUserId(userId, pageable).map(loginHistoryMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Closed a login history session (logout)")
    public LoginHistoryResponse update(UUID id, UpdateLoginHistoryRequest request) {
        LoginHistory entity = getEntityOrThrow(id);
        loginHistoryMapper.updateEntityFromRequest(request, entity);
        LoginHistory saved = loginHistoryRepository.save(entity);
        return loginHistoryMapper.toResponse(saved);
    }

    private LoginHistory getEntityOrThrow(UUID id) {
        return loginHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoginHistory", id));
    }
}
