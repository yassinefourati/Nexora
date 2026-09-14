package com.fourati.identity;

import java.io.Serializable;
import java.util.UUID;

/** The backend identity a token maps to. Serializable so it can live in the shared cache. */
public record ProvisionedUser(UUID id, String username) implements Serializable {
}
