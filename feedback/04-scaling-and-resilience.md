# Scaling and resilience notes

Single-instance-only assumptions baked into the current design. None of this
is urgent if you're running one backend replica (which the current compose
setup does), but it's worth knowing about before you ever scale out.

## Things that only work correctly with exactly one backend replica

1. **Rate limiting** (`RateLimitFilter`) — in-memory `ConcurrentHashMap` of
   token buckets, per JVM.
2. **Idempotency cache** (`IdempotencyFilter`) — same, in-memory, per JVM.
3. **WebSocket broker** (`WebSocketConfig`'s `enableSimpleBroker`) — Spring's
   built-in broker only knows about sessions connected to the same JVM.

See `02-security-followups.md` items 3–4 for the detail. The fix for all
three is the same shape: move shared state to something external (Redis for
1–2, a STOMP relay like RabbitMQ for 3).

## No retry/timeout policy on external calls

- The JWKS fetch (Spring Security's OAuth2 resource server pulling
  Keycloak's signing keys) uses Spring's defaults — no explicit timeout
  configured, no retry/circuit-breaker. If Keycloak is briefly unreachable
  during a JWKS refresh, requests will hang for however long the default
  HTTP client timeout is rather than failing fast.
- There's no `Resilience4j` or equivalent anywhere in the dependency tree.
  If you ever add an outbound call to something else (an email provider for
  `notification_templates`, a webhook, S3 for `Attachment` storage instead
  of local disk), it'll need its own timeout/retry/circuit-breaker story —
  there's no existing pattern in this codebase to follow.

## Refresh_user_permissions_mv() is dead

`persistence/.../V13__real_schema_functions_triggers.sql` defines
`refresh_user_permissions_mv()` as a Postgres function, and
`user_permissions_mv` is a materialized view backing
`UserPermissionsViewRepository`. Nothing calls the refresh function —
not a scheduled job, not a trigger, nothing. So `user_permissions_mv` is
frozen at whatever it contained right after migration. Either:

- add a `@Scheduled` job (there's precedent —
  `RateLimitFilter.evictStaleBuckets` and `IdempotencyFilter.evictExpired`
  already use `@Scheduled`) that calls `refresh_user_permissions_mv()` on an
  interval, or
- refresh it via a trigger on `user_roles`/`role_permissions` changes
  (`REFRESH MATERIALIZED VIEW CONCURRENTLY` inside a trigger, or via
  `pg_cron` if you don't want it synchronous with the write), or
- drop the materialized view and the repository backing it if nothing
  actually reads `UserPermissionsView` yet (worth checking — I didn't trace
  every caller).

## Notifications aren't durable

`UserNotificationService.create` does a DB write, then (after my fix) pushes
over WebSocket after commit. If the recipient isn't connected when the push
fires, they only find out on their next `GET /user-notifications` poll — no
outbox/retry, no push-to-email fallback. `notification_templates` exists in
the schema (with a `channel` column suggesting email was intended) but
there's no code anywhere that sends an email. If notification delivery
matters beyond "logged in and happened to be looking," this needs a real
outbox pattern: write the notification + a `pending_delivery` row in the
same transaction, a worker that processes the outbox and tries WebSocket
push, falling back to email via whatever provider you pick, with retry and
a dead-letter path for permanent failures.
