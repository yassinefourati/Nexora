/**
 * Configuration domain: settings, feature flags, application modules and
 * metadata key-values. Rows scoped to an organization hold its id and validate
 * it through {@code OrganizationFacade}.
 *
 * <p>Public API (this package): the request and response records. Everything
 * under {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Configuration")
package com.fourati.configuration;

import org.springframework.modulith.ApplicationModule;
