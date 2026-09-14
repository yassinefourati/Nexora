package com.fourati.integration.support;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.application.CreateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Test-only bean that wraps an audited write in an outer transaction which
 * then fails, so integration tests can prove that a rolled-back business
 * write leaves no audit row behind (ADR 0007). Lives in test sources; it is
 * not a controller, so it never reaches the OpenAPI snapshot.
 */
@Component
@RequiredArgsConstructor
public class TransactionalScenario {

    private final CreateUser createUser;

    /** Creates the user (audited), then throws so the whole transaction rolls back. */
    @Transactional
    public UUID createUserThenFail(CreateUserRequest request) {
        UserResponse created = createUser.execute(request);
        throw new SimulatedFailure(created.id());
    }

    public static final class SimulatedFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final UUID userId;

        SimulatedFailure(UUID userId) {
            super("simulated failure after audited write of user " + userId);
            this.userId = userId;
        }

        public UUID userId() {
            return userId;
        }
    }
}
