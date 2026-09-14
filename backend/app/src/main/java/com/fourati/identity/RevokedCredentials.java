package com.fourati.identity;

/** Outcome of {@link IdentityFacade#revokeCredentialsAndSuspend}: what was revoked and the resulting account status. */
public record RevokedCredentials(int sessions, int apiKeys, String status) {
}
