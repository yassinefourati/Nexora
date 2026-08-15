package com.fourati.integration;

import com.fourati.domain.User;
import com.fourati.dto.request.CreateUserRequest;
import com.fourati.dto.request.UpdateUserRequest;
import com.fourati.dto.response.UserResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.UserRepository;
import com.fourati.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers), covering the
 * core Users CRUD flow through the real service -> repository -> database path (not
 * mocks). Exercises the same soft-delete + password-hashing + conflict-detection
 * behavior a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class UserCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CreateUserRequest newUserRequest(String suffix) {
        return new CreateUserRequest(
                "crud-test-" + suffix,
                "crud-test-" + suffix + "@example.com",
                "InitialPassword123!",
                "Test",
                "User",
                "active",
                false);
    }

    @Test
    void create_persistsHashedPasswordAndIsRetrievableById() {
        String suffix = UUID.randomUUID().toString();
        CreateUserRequest request = newUserRequest(suffix);

        UserResponse created = userService.create(request);

        assertThat(created.id()).isNotNull();

        User stored = userRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getUsername()).isEqualTo(request.username());
        // Password is never stored in plaintext — only its hash, verifiable via the encoder.
        assertThat(stored.getPasswordHash()).isNotEqualTo(request.password());
        assertThat(passwordEncoder.matches(request.password(), stored.getPasswordHash())).isTrue();

        userRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsDuplicateUsername() {
        String suffix = UUID.randomUUID().toString();
        UserResponse first = userService.create(newUserRequest(suffix));

        assertThatThrownBy(() -> userService.create(newUserRequest(suffix)))
                .isInstanceOf(ConflictException.class);

        userRepository.deleteById(first.id());
    }

    @Test
    void update_changesFieldsAndPersists() {
        UserResponse created = userService.create(newUserRequest(UUID.randomUUID().toString()));

        UpdateUserRequest update = new UpdateUserRequest(
                "updated-" + created.id() + "@example.com",
                "Updated",
                "Name",
                "inactive",
                false);

        UserResponse updated = userService.update(created.id(), update);

        assertThat(updated.email()).isEqualTo(update.email());
        assertThat(updated.firstName()).isEqualTo("Updated");
        assertThat(updated.status()).isEqualTo("inactive");

        User stored = userRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getEmail()).isEqualTo(update.email());

        userRepository.deleteById(created.id());
    }

    @Test
    void delete_makesUserUnreadableThroughTheNormalReadPath() {
        UserResponse created = userService.create(newUserRequest(UUID.randomUUID().toString()));

        userService.delete(created.id());

        // User carries @SQLRestriction("deleted_at IS NULL"), which Hibernate applies to
        // every query against the entity — including findById — not just findAll/list
        // queries. So after a soft delete, both the service and the repository report
        // "not found" through their normal read paths; there is no way to tell a soft
        // delete apart from a hard delete without a native (non-Hibernate) query.
        assertThatThrownBy(() -> userService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(userRepository.findById(created.id())).isEmpty();
    }
}
