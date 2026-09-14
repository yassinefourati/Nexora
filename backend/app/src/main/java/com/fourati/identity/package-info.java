/**
 * Identity domain: users and their identity-provider links (just-in-time
 * provisioning, ADR 0001), API keys, sessions, login history and auth logs,
 * plus the ownership checks used by {@code @PreAuthorize} expressions.
 *
 * <p>Public API (this package): the request and response records,
 * {@link com.fourati.identity.IdentityFacade} with its records, and the
 * {@link com.fourati.identity.IdentityChanged} event. Everything under
 * {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Identity")
package com.fourati.identity;

import org.springframework.modulith.ApplicationModule;
