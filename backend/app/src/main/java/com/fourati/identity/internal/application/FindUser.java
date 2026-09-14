package com.fourati.identity.internal.application;

import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Query: one user by id, 404 when unknown or soft-deleted. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUser {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse execute(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
