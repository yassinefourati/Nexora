/**
 * Access domain: roles, permissions, their grants to roles and to users,
 * menus and menu permissions, and the access-management use cases (assign a
 * role, grant permissions, revoke all access, effective permissions).
 *
 * <p>Public API (this package): the request and response records,
 * {@link com.fourati.access.AccessFacade} and the
 * {@link com.fourati.access.AccessChanged} event. Everything under
 * {@code internal} is private to the module (ADR 0004).
 */
@ApplicationModule(displayName = "Access")
package com.fourati.access;

import org.springframework.modulith.ApplicationModule;
