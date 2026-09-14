package com.fourati.identity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Lifecycle of a user account. Stored and exposed as the lowercase value
 * ({@code active}, ...) so the database check constraint (V18) and the API
 * contract stay what they were; the Java code only ever sees the enum.
 */
public enum UserStatus {

    /** May sign in and be granted roles. */
    ACTIVE("active"),
    /** Deliberately switched off by an administrator; may be reactivated. */
    INACTIVE("inactive"),
    /** Cut off by {@code RevokeUserAccess} or {@code SuspendUser}; cannot be granted roles until reactivated. */
    SUSPENDED("suspended"),
    /** Created by an administrator, has not signed in yet. */
    PENDING("pending");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static UserStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(s -> s.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user status: " + value));
    }
}
