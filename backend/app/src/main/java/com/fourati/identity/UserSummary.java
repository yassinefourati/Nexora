package com.fourati.identity;

import java.util.UUID;

/** The little other modules need to know about a user. */
public record UserSummary(UUID id, String username, UserStatus status) {

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
