package com.fourati.dto.request;

import java.time.Instant;

/**
 * Used to close an open login session by recording the logout timestamp.
 */
public record UpdateLoginHistoryRequest(
        Instant logoutAt
) {
}
