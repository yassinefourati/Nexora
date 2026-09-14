package com.fourati.identity.internal.application;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.UserStatus;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.error.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Rules of the pre-provisioning use case: unique username and email, nothing about passwords. */
@ExtendWith(MockitoExtension.class)
class CreateUserTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private CreateUser createUser;

    private static CreateUserRequest request() {
        return new CreateUserRequest("alice", "alice@example.com", "Alice", "Admin", UserStatus.ACTIVE, false);
    }

    @Test
    void duplicateUsername_isAConflict_andNothingIsSaved() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> createUser.execute(request())).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicateEmail_isAConflict_andNothingIsSaved() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> createUser.execute(request())).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void newUser_isMappedSavedAndReturned() {
        User entity = new User();
        UserResponse response = new UserResponse(null, "alice", "alice@example.com", "Alice", "Admin",
                UserStatus.ACTIVE, false, null, null, null, null);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(response);

        assertThat(createUser.execute(request())).isSameAs(response);
    }
}
