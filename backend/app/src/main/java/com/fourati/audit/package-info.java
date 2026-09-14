/**
 * Audit domain: the audit trail (written in the business transaction by the
 * listener on {@code AuditEvent}, ADR 0007), system events, error logs, the
 * monthly partition and retention job, and the exports. Actors are held as
 * user ids; the identity module owns users.
 *
 * <p>Public API (this package): the request and response records. Everything
 * under {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Audit")
package com.fourati.audit;

import org.springframework.modulith.ApplicationModule;
