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

/** Use case: reactivate a suspended, inactive or pending account. */
@Service
@RequiredArgsConstructor
@Transactional
public class ActivateUser {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.USER_EDIT)
    @Audited(action = "ACTIVATE", entityType = "user", description = "Activate a user")
    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.activate();
        eventPublisher.publishEvent(new IdentityChanged(id, "ACTIVATED"));
        return userMapper.toResponse(userRepository.save(user));
    }
}
