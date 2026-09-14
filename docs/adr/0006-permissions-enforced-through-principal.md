# ADR 0006: The database permission model is enforced through authorities on the principal

Status: accepted (2026-09-13); implemented in Phase 5 the same day (permission codes become authorities at authentication, cached per user, invalidated on AccessChanged; the materialized view is gone in V19).

## Context

Two authorization models exist and only one is enforced:

- Every `@PreAuthorize` uses `hasRole('ADMIN')` or `isAuthenticated()`,
  derived from the Keycloak `roles` claim, plus ownership checks for
  user-owned rows.
- The schema carries `permissions`, `role_permissions`, `menu_permissions`
  and the `user_permissions_mv` materialized view. The only reader is
  `GET /me/permissions`, which the frontend uses to show or hide menu
  entries. Nothing on the backend checks a permission, and the view is
  refreshed every five minutes for nobody.

Identity is also resolved repeatedly per request: `UserProvisioningFilter`
links the subject to a user (cached), then `CurrentUserResolver` queries the
database again in every ownership check, the audit listener, session checks
and `/me`, because the `Authentication` object carries only the JWT.

## Decision

- A `CurrentUser` principal (`userId`, `subject`, `username`, `roles`,
  `permissions`) is built **once** per request by the JWT and API-key
  authentication converters. Just-in-time provisioning happens there.
  `UserProvisioningFilter` and `CurrentUserResolver` are removed. Ownership
  checks compare against `principal.userId()` with no database call.
- Keycloak roles remain coarse (`ADMIN`, `USER`). Fine-grained permissions
  come from the database: the effective permission set for the user's roles
  is loaded at authentication, cached in Redis keyed by role set, and exposed
  as `GrantedAuthority` values equal to the permission code, `<resource>.<action>` (the form the `permissions` table generates).
- Use cases and endpoints are guarded with
  `hasAuthority('<resource>.<action>')` through the constants of `shared.security.Authorize`. `hasRole('ADMIN')` remains only as
  a super-user short-circuit.
- An `AccessChanged` event is published whenever a role grant or
  role-permission grant changes. Every replica evicts the affected cache
  entries on that event (see ADR 0008).
- `user_permissions_mv` and its refresh job are dropped once no reader
  remains.

## Consequences

- The permission tables become the real authorization model, and the
  administration UI that edits them has an effect.
- A permission change is visible on the next request after cache eviction,
  which is at most the event propagation delay.
- Rejected alternative: delete the permission tables and rely on Keycloak
  roles only. Simpler, but it removes the fine-grained model the product
  already exposes in its UI and would move every permission edit into
  Keycloak.
