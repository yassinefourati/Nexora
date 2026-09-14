package com.fourati.identity;

import java.util.List;
import java.util.UUID;

/** What an authenticated API key grants: its owner and its scopes. */
public record ApiKeyPrincipal(UUID apiKeyId, UUID userId, String username, List<String> scopes) {
}
