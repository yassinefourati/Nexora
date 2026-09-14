package com.fourati.identity.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.identity.IdentityChanged;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: suspend an account. A suspended user keeps their rows and role
 * grants but cannot be granted new roles until {@link ActivateUser} runs.
 * Cutting every credential as well is {@code RevokeUserAccess}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SuspendUser {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.USER_EDIT)
    @Audited(action = "SUSPEND", entityType = "user", description = "Suspend a user")
    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.suspend();
        eventPublisher.publishEvent(new IdentityChanged(id, "SUSPENDED"));
        return userMapper.toResponse(userRepository.save(user));
    }
}
