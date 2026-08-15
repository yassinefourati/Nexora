package com.fourati.service;

import com.fourati.domain.User;
import com.fourati.dto.request.CreateUserRequest;
import com.fourati.dto.response.UserResponse;
import com.fourati.mapper.UserMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies UserService uses the injected shared PasswordEncoder bean (rather than
 * hand-instantiating one) to hash passwords on create(), and the username/email
 * conflict guards.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest newRequest() {
        return new CreateUserRequest(
                "jdoe",
                "jdoe@example.com",
                "raw-password-123",
                "Jane",
                "Doe",
                "active",
                false
        );
    }

    @Test
    void create_hashesPasswordViaInjectedPasswordEncoder() {
        CreateUserRequest request = newRequest();
        User entity = new User();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-value");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(
                new UserResponse(null, "jdoe", "jdoe@example.com", "Jane", "Doe",
                        "active", false, 0, null, null, null, null, null));

        userService.create(request);

        verify(passwordEncoder).encode(eq("raw-password-123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getPasswordHash()).isEqualTo("hashed-value");
        assertThat(saved.getPasswordHash()).isNotEqualTo(request.password());
        assertThat(saved.getPasswordChangedAt()).isNotNull();
    }

    @Test
    void create_throwsConflict_whenUsernameAlreadyExists() {
        CreateUserRequest request = newRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void create_throwsConflict_whenEmailAlreadyExists() {
        CreateUserRequest request = newRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
