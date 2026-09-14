/**
 * Notification domain: notifications, their templates and per-user delivery
 * rows, plus the live WebSocket push after commit. Recipients are held as user
 * ids; the identity module owns users.
 *
 * <p>Public API (this package): the request and response records and
 * {@link com.fourati.notification.NotificationFacade}. Everything under
 * {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Notification")
package com.fourati.notification;

import org.springframework.modulith.ApplicationModule;
