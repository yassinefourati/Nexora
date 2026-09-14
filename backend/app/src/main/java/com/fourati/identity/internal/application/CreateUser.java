package com.fourati.identity.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: an administrator pre-provisions a user so roles can be granted
 * before the person first signs in through Keycloak (ADR 0001). The row is
 * linked to its identity-provider subject on first sign-in.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateUser {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PreAuthorize(Authorize.USER_WRITE)
    @Audited(action = "CREATE", entityType = "user", description = "Create a new user")
    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("User already exists with username: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email());
        }
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }
}
