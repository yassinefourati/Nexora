/**
 * Shared kernel of the application: JPA base entities, the optimistic-lock
 * version check and API constants that every domain module uses. An
 * <em>open</em> Spring Modulith module with no business logic and no
 * dependency on any domain module (ADR 0004).
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN, displayName = "Shared kernel")
package com.fourati.shared;

import org.springframework.modulith.ApplicationModule;
