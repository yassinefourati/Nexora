package com.fourati.identity.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.identity.IdentityChanged;
import com.fourati.identity.UpdateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.shared.persistence.VersionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Use case: change a user's profile, superuser flag or status, refusing a stale write with 409. */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateUser {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.USER_EDIT)
    @Audited(action = "UPDATE", entityType = "user", description = "Update a user")
    public UserResponse execute(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        VersionCheck.check(User.class, id, user.getVersion(), request.version());
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email());
        }
        userMapper.updateEntityFromRequest(request, user);
        user.changeStatus(request.status());
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new IdentityChanged(id, "UPDATED"));
        return userMapper.toResponse(saved);
    }
}
