/**
 * Organization domain: organizations, their members, departments and teams.
 *
 * <p>Public API (this package): the request and response records and
 * {@link com.fourati.organization.OrganizationFacade}. Everything under
 * {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Organization")
package com.fourati.organization;

import org.springframework.modulith.ApplicationModule;
