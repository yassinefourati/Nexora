package com.fourati.access;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Body of {@code POST /roles/{id}/permissions}. Grants every listed permission
 * in one transaction; permissions the role already holds are skipped, not
 * rejected, so the call is safe to repeat.
 */
public record GrantPermissionsRequest(
        @NotEmpty @Size(max = 500) List<UUID> permissionIds
) {
}
