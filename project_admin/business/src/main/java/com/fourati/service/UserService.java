package com.fourati.service;

import com.fourati.domain.User;
import com.fourati.dto.request.CreateUserRequest;
import com.fourati.dto.request.UpdateUserRequest;
import com.fourati.dto.response.UserResponse;
import com.fourati.mapper.UserMapper;
import com.fourati.repository.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Audited(action = "CREATE", description = "Create a new user")
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("User already exists with username: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email());
        }
        User entity = userMapper.toEntity(request);
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setPasswordChangedAt(Instant.now());
        User saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a user")
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User entity = getEntityOrThrow(id);
        if (!entity.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email());
        }
        userMapper.updateEntityFromRequest(request, entity);
        User saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a user")
    public void delete(UUID id) {
        User entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        userRepository.save(entity);
    }

    private User getEntityOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
