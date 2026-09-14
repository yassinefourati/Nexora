/**
 * Cross-cutting platform code, exposed as an <em>open</em> Spring Modulith
 * module: every sub-package (error, security, audit, web, export, ...) is
 * public API for the domain modules. The platform never depends on a domain
 * module; the ArchUnit rule {@code platform_classes_must_not_depend_on_domain_code}
 * and the Modulith verification both enforce that direction (ADR 0004).
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN, displayName = "Platform")
package com.fourati.platform;

import org.springframework.modulith.ApplicationModule;
