# ADR 0001: Keycloak owns authentication; the backend links users by subject

Status: accepted (2026-09-13)

## Context

The backend started life with its own `users.password_hash`, `failed_login_attempts`
and `locked_until` columns, a `BCryptPasswordEncoder`, and a Keycloak OIDC
resource-server configuration. Nothing ever authenticated against the local
password: every request carries a Keycloak JWT. The two identity stores were
linked only by matching `preferred_username` strings, which breaks the moment
a user is renamed in Keycloak and produced foreign-key failures in the audit
log when the JWT subject was mistaken for `users.id`.

## Decision

- Keycloak is the only authentication authority. The API validates JWTs
  (Authorization Code + PKCE from the SPA, password grant only on the dev-only
  `load-test` client) and never verifies a local password.
- `users.password_hash` is nullable (V15) and no endpoint requires a password.
- Each Keycloak subject is linked to exactly one `users` row through
  `user_identity_providers(provider='keycloak', provider_user_id=<sub>)`.
  `UserProvisioningService` creates the link on first request (JIT), reusing a
  legacy row with the same username when one exists, otherwise creating the row
  from the token's profile claims.
- Every place that needs "the current user" goes through `CurrentUserResolver`
  (subject first, username only as a fallback for pre-link tokens): ownership
  checks, session ownership, the audit actor, the WebSocket principal, `/me`.
- Programmatic access uses `X-Api-Key` (hashed at rest, scope-based
  authorities, never `ROLE_ADMIN`), authenticated by `ApiKeyAuthenticationFilter`.

## Consequences

- Renaming or re-provisioning a user in Keycloak does not orphan their data.
- The local password columns remain for historical rows and can be dropped in a
  later migration once no environment depends on them.
- Admin-side user creation still exists (`POST /users`) for pre-provisioning
  roles before first login; the row is linked when the person signs in.
- Rejected alternative: a Keycloak admin-event webhook for provisioning. It
  needs Keycloak-side configuration per environment and a public endpoint;
  JIT provisioning needs neither and covers the same cases.
