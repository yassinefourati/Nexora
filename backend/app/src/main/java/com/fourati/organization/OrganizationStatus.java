package com.fourati.organization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/** Lifecycle of an organization; lowercase on the wire and in the database (check constraint from V2). */
public enum OrganizationStatus {

    ACTIVE("active"),
    INACTIVE("inactive"),
    ARCHIVED("archived");

    private final String value;

    OrganizationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static OrganizationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(s -> s.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown organization status: " + value));
    }
}
