package com.fourati.integration;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UpdateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.UserStatus;
import com.fourati.identity.internal.application.ActivateUser;
import com.fourati.identity.internal.application.CreateUser;
import com.fourati.identity.internal.application.DeleteUser;
import com.fourati.identity.internal.application.FindUser;
import com.fourati.identity.internal.application.SuspendUser;
import com.fourati.identity.internal.application.UpdateUser;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The user lifecycle through the real use cases against Postgres: creation
 * with a defaulted status, uniqueness, profile update with an explicit status
 * transition, suspend/activate, and soft delete.
 */
class UserCrudIntegrationTest extends AbstractIntegrationTest {

    @Autowired private CreateUser createUser;
    @Autowired private UpdateUser updateUser;
    @Autowired private DeleteUser deleteUser;
    @Autowired private SuspendUser suspendUser;
    @Autowired private ActivateUser activateUser;
    @Autowired private FindUser findUser;
    @Autowired private UserRepository userRepository;

    /** The use cases are guarded (ADR 0006); these tests exercise them as an administrator. */
    @BeforeEach
    void asAdmin() {
        SecurityContextHolder.getContext().setAuthentication(TestAuthentications.token(null, "root", "admin"));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private CreateUserRequest newUserRequest(String suffix) {
        return new CreateUserRequest("crud-test-" + suffix, "crud-test-" + suffix + "@example.com",
                "Test", "User", null, false);
    }

    @Test
    void create_defaultsStatusToActive_andIsRetrievableById() {
        UserResponse created = createUser.execute(newUserRequest(UUID.randomUUID().toString()));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(UserStatus.ACTIVE);
        User stored = userRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getUsername()).isEqualTo(created.username());
        assertThat(stored.getStatus()).isEqualTo(UserStatus.ACTIVE);

        userRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsDuplicateUsername() {
        String suffix = UUID.randomUUID().toString();
        UserResponse first = createUser.execute(newUserRequest(suffix));

        assertThatThrownBy(() -> createUser.execute(newUserRequest(suffix)))
                .isInstanceOf(ConflictException.class);

        userRepository.deleteById(first.id());
    }

    @Test
    void update_changesFieldsAndStatus_andPersists() {
        UserResponse created = createUser.execute(newUserRequest(UUID.randomUUID().toString()));

        UpdateUserRequest update = new UpdateUserRequest(
                "updated-" + created.id() + "@example.com", "Updated", "Name", UserStatus.INACTIVE, false, null);

        UserResponse updated = updateUser.execute(created.id(), update);

        assertThat(updated.email()).isEqualTo(update.email());
        assertThat(updated.firstName()).isEqualTo("Updated");
        assertThat(updated.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(userRepository.findById(created.id()).orElseThrow().getStatus()).isEqualTo(UserStatus.INACTIVE);

        userRepository.deleteById(created.id());
    }

    @Test
    void suspend_thenActivate_movesTheAccountThroughItsLifecycle() {
        UserResponse created = createUser.execute(newUserRequest(UUID.randomUUID().toString()));

        assertThat(suspendUser.execute(created.id()).status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(findUser.execute(created.id()).status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(activateUser.execute(created.id()).status()).isEqualTo(UserStatus.ACTIVE);

        userRepository.deleteById(created.id());
    }

    @Test
    void delete_makesUserUnreadableThroughTheNormalReadPath() {
        UserResponse created = createUser.execute(newUserRequest(UUID.randomUUID().toString()));

        deleteUser.execute(created.id());

        // User carries @SQLRestriction("deleted_at IS NULL"), so after a soft delete both the
        // use case and the repository report "not found" through their normal read paths.
        assertThatThrownBy(() -> findUser.execute(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(userRepository.findById(created.id())).isEmpty();
    }
}
