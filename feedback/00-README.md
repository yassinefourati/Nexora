# Feedback notes

These files were written after the first review as notes on what still needed
doing. The "do it all" pass has since implemented the substance of every note;
they are kept as the record of *why* each change exists. The current state of
the system is documented in `docs/` (architecture, ADRs, runbooks), not here.

Files:

- `01-dead-code-to-delete.md` — done: everything listed is deleted or wired.
- `02-security-followups.md` — done: API-key authentication, subject-linked
  identity with JIT provisioning, single async audit path, Redis-backed
  limiter/idempotency, RabbitMQ relay, management port for metrics.
- `03-architecture-and-domain-logic.md` — done: `usecase/rbac` (assign role,
  bulk grant, revoke all access, effective permissions), stable `ErrorCode`s,
  `version` carried end to end with 409 on stale writes, identity resolved by
  subject everywhere. Not done: cutting the generic CRUD surface, which is a
  product decision.
- `04-scaling-and-resilience.md` — done: two-replica compose profile proven
  with a shared rate-limit budget and a k6 run, JWKS circuit breaker, MV
  refresh job. Not done: a notification outbox (no email channel exists yet).
- `05-testing-gaps.md` — done: HTTP authorization matrix, use-case and
  circuit-breaker unit tests, MSW-based frontend tests for the interceptor,
  route guard and user form, Playwright against the real stack (22 passing).
- `06-devops-and-ci.md` — done: CI (verify, lint/test/build, Trivy, e2e on
  the compose stack, signed images, SBOM, staging deploy), rollback workflow,
  Kustomize base + dev/staging/prod overlays, runtime frontend config.
- `07-frontend-notes.md` — done, and extended in the 20/20 pass: every list
  screen is the shared `CrudPage` (server-side `?q=` search on 20 backend
  endpoints, skeleton/error/empty states, permission gating), the whole UI is
  translated (en/fr, lint-enforced, locale parity test), the API client is
  lint-fenced out of UI code, dev pages and the client-side PDF export are out
  of the production bundle (3.2 MB → 1.4 MB), and keyboard/axe tests cover the
  shared table and row menu. Not done: unifying paginated/unpaginated filtered
  list responses.
- `08-data-model-notes.md` — done: optimistic locking, MV refresh, single
  audit path, audit-log partition maintenance and retention, backup/restore
  rehearsed and recorded.
- `09-quick-wins.md` — done.

## Verified on 2026-09-13

- Backend: 85 unit + architecture tests green after a clean build; the three
  Testcontainers suites are run by CI (the Docker client on this Windows host
  cannot reach the daemon from JUnit, a known local limitation).
- Frontend: lint clean, `tsc` clean, 62 Vitest tests green, 22 Playwright
  tests green against the real compose stack.
- Runtime: two API replicas behind nginx with Redis + RabbitMQ; shared
  rate-limit budget proven (127 allowed of 300 at 120 rpm across both
  replicas); k6 20 VUs × 60 s: 9 233 requests, 0 failures, p95 ≤ 15 ms;
  backup 1 s / restore 1 s rehearsal recorded in `docs/runbooks/backup-restore.md`.
- Found and fixed during verification: a pool deadlock from the synchronous
  after-commit audit write (30 s stalls under load), a lazy-load error on
  `/me`, Kotlin leaking into the jar via the OTLP sender and breaking
  `/v3/api-docs`, tag/notification writes open to any principal, the route
  guard bouncing to 403 while permissions loaded, Vitest running Playwright
  specs, and the frontend runtime-config hook lacking write permission.
