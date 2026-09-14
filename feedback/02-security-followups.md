# Security follow-ups

What's still open after the IDOR/rate-limit/idempotency/CORS/headers pass.
Roughly ordered by how much I'd worry about each.

## 1. API keys are issued but never authenticate anything

`ApiKeyService` generates a secret, hashes it with SHA-256, and stores only
the hash — that part is right. But nothing on the request path ever reads
`key_hash`. There's no `ApiKeyAuthenticationFilter`, no `X-Api-Key` header
handling anywhere in `SecurityConfig`. So the feature exists in the database
and the UI (`frontend/src/features/apiKeys/`) but is functionally inert —
anyone holding an "API key" can't actually use it to call the API.

Either:
- **Finish it**: a filter before `BearerTokenAuthenticationFilter` that reads
  `X-Api-Key`, hashes it, looks it up, checks `revoked_at`/`expires_at`,
  builds authorities from `scopes`, and updates `last_used_at`
  asynchronously (don't block the request on that write).
- **Or remove the feature** end to end (entity, service, controller,
  frontend page) if programmatic access isn't actually a near-term need.

Half-built auth features are worse than no auth feature — they look like
something someone can rely on.

## 2. Two identity systems, only loosely linked

`users.password_hash` exists (now nullable after the last patch), Keycloak
also has its own user store, and the two are linked *only* by matching
`username` strings — not by a stored `keycloak_sub` foreign key, even though
a `user_identity_providers` table already exists in the schema for exactly
this purpose. Several comments in the code say "confirmed live: Keycloak
`sub` and `users.id` are different UUIDs" as if this was a real incident.

I made the local password optional so accounts can be Keycloak-only, but I
did not:
- add JIT provisioning (a first-authenticated-request hook that creates the
  backend `users` row from the JWT if it doesn't exist yet), so today a
  Keycloak user with no matching `users` row by username silently 404s on
  `/me` and everything that depends on it
- wire `user_identity_providers` to actually store the Keycloak `sub`, so
  identity resolution stops depending on username matching (which breaks if
  someone is ever renamed in Keycloak)

This is the single biggest "this will bite you in real use" item in the
repo. It's a genuine design decision (webhook-based provisioning vs.
lazy-create-on-first-request vs. requiring admin pre-provisioning), so I
didn't want to pick for you.

## 3. Rate limiting and idempotency state is per-JVM

I fixed the *correctness* bugs (XFF spoofing, cross-user idempotency replay)
but didn't change the fact that both live in an in-memory `ConcurrentHashMap`
per instance. This is fine for a single replica. The moment you run two
backend pods behind a load balancer:
- each pod enforces its own rate-limit budget, so the effective limit is
  `rpm × replica count`
- idempotency keys aren't shared, so a retry that lands on a different pod
  reprocesses instead of returning the cached response

If you ever go multi-replica, this needs Redis (Bucket4j for rate limiting,
a simple `SETNX`-style key for idempotency) or a Postgres table. Until then,
it's fine — just don't scale the backend horizontally without addressing
this first.

## 4. WebSocket broker is also single-instance

Same category: `registry.enableSimpleBroker(...)` in `WebSocketConfig` is
Spring's in-memory broker. Two replicas means a client connected to pod A
never receives a push destined for a user whose session landed on pod B (the
`UserNotificationService.create` push goes out via `SimpMessagingTemplate`,
which only knows about locally-connected sessions). If you scale this out,
you need a real message broker relay (RabbitMQ STOMP plugin is the standard
pairing with Spring's `enableStompBrokerRelay`).

## 5. Audit trigger writes rows with no actor

`V13__real_schema_functions_triggers.sql` installs `fn_audit_trigger()` on
six tables (`permissions`, `role_permissions`, `roles`, `settings`,
`user_roles`, `users`). It tries to read `current_setting('app.current_user_id')`
but nothing in the Java code ever calls `SET LOCAL app.current_user_id`, so
every trigger-generated audit row has `user_id = NULL`. Meanwhile the
`@Audited`/`AuditAspect` path (which I moved to fire after commit) writes a
*second*, separate audit row for the same write, this one with a real actor.

So today every audited write to those six tables produces two `audit_logs`
rows: one correct, one anonymous. Pick one mechanism:

- **Keep the trigger, wire the actor**: add a
  `TransactionSynchronization`/connection interceptor that issues
  `SET LOCAL app.current_user_id = '<uuid>'` at the start of each write
  transaction. The trigger then gets before/after JSON diffs "for free,"
  which the aspect-based approach doesn't do today.
- **Keep the aspect, drop the triggers**: simpler, but loses the
  automatic before/after diff and only covers methods explicitly annotated
  `@Audited` (so a raw repository `save()` call that bypasses the service
  layer wouldn't be audited).

I'd lean toward keeping the trigger and wiring the actor — it's the only
mechanism that can't be bypassed by forgetting an annotation — but that's a
judgment call, not something to decide unasked.

## 6. Actuator surface beyond /health

I made `/actuator/health` details `when-authorized`, but
`management.endpoints.web.exposure.include: health,info,metrics,prometheus`
still exposes `/actuator/metrics` and `/actuator/prometheus` on the public
path list check — actually, checking `SecurityConfig.PUBLIC_PATHS`, only
`/actuator/health/**` and `/actuator/info` are explicitly public, so
`/metrics` and `/prometheus` fall through to `.anyRequest().authenticated()`
and are *not* actually public. Good — but worth double-checking in a real
deployment that your ingress/network policy doesn't separately expose the
whole `/actuator/**` prefix (Prometheus scraping often gets configured with
a network-level bypass that ignores the app's own auth). If you add a
scraper, it should hit `/actuator/prometheus` with its own auth (basic auth
or mTLS on that one path), not an app-wide bypass.

## 7. Swagger / OpenAPI docs are public in every profile except prod

`springdoc.swagger-ui.enabled: false` and `api-docs.enabled: false` only in
`application-prod.yml`. That's correct for a real prod deployment, but if you
ever run a staging/demo environment on the `dev` or `docker` profile with a
public URL, the full API surface (including request/response shapes for
every one of the 33 resources) is unauthenticated-readable. Consider a
`staging` profile that behaves like `prod` on this point but keeps other dev
conveniences.

## 8. CSP still allows `'unsafe-inline'` for styles

`frontend/nginx.conf`'s CSP has `style-src 'self' 'unsafe-inline'` because
MUI/Emotion needs it. This is a known, common trade-off (Emotion's
CSS-in-JS injects `<style>` tags), not a bug — flagging it so it's a
documented decision rather than something that looks accidental. If you ever
want to close it, MUI supports a nonce-based Emotion cache
(`@emotion/cache` with a CSP nonce), but it's a real integration effort, not
a config flip.
