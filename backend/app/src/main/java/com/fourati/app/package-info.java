/**
 * Application shell: bootstrap configuration, the security filter chain and
 * its adapters (JWT converter, API-key filter, problem-details entry points,
 * WebSocket authentication), caching and scheduling wiring, OpenAPI, and the
 * developer error catalog. Depends on the domain modules' public APIs and on
 * the platform; no domain module depends on it (ADR 0004).
 */
@ApplicationModule(displayName = "Application shell")
package com.fourati.app;

import org.springframework.modulith.ApplicationModule;
