package com.fourati.organization;

import com.fourati.platform.error.ResourceNotFoundException;

import java.util.UUID;

/**
 * What other modules may ask the organization domain. Modules that scope a
 * row to an organization (settings, feature flags) hold the organization id
 * and validate it here instead of loading the entity (ADR 0004).
 */
public interface OrganizationFacade {

    boolean exists(UUID organizationId);

    /** Fails with the standard 404 problem when the organization does not exist. */
    default void ensureExists(UUID organizationId) {
        if (!exists(organizationId)) {
            throw new ResourceNotFoundException("Organization", organizationId);
        }
    }
}
