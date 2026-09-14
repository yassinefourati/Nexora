# ADR 0008: Shared cache, scheduler locking and build-time guarantees for multi-replica deployments

Status: accepted (2026-09-13)

## Context

The API is designed to run as interchangeable replicas (Redis-backed rate
limiting and idempotency, RabbitMQ STOMP relay). Three pieces of state did
not follow:

- `effectivePermissions` and `provisionedUsers` are Caffeine caches local
  to each JVM. `@CacheEvict(allEntries = true)` after a grant or revoke
  evicts only on the replica that handled the write; the others serve stale
  permissions for up to five minutes. A revoked user keeps their access on
  every other replica.
- `AuditLogPartitionJob` and `PermissionsViewRefreshJob` (retired in Phase 5,
  ADR 0006) are plain `@Scheduled` methods. Every replica runs them at the
  same time; a concurrent `REFRESH MATERIALIZED VIEW CONCURRENTLY` fails on
  the loser and the failure is swallowed as a warning.
- The architectural rules (ArchUnit, and the Modulith verification from
  ADR 0004) are tests. The CI workflow was removed to avoid Actions billing,
  so nothing runs them except a developer typing `make verify`.

## Decision

- **Cache**: Spring Cache is backed by Redis for every cache that influences
  authorization or identity (`effectivePermissions`, `provisionedUsers`).
  Eviction is driven by events (`AccessChanged`, ADR 0006) delivered to
  every replica, not by a local `@CacheEvict`. A local Caffeine layer in
  front of Redis is allowed only with the same event-driven invalidation and
  only if a measurement shows the Redis round trip matters.
- **Scheduling**: every `@Scheduled` method is annotated with
  `@SchedulerLock` (ShedLock, JDBC provider, `shedlock` table owned by
  Flyway). Exactly one replica runs a job per interval.
- **Guarantees**: `scripts/git-hooks/pre-push` runs the backend `verify`
  before every push; `make hooks` installs it. Bypass is explicit
  (`SKIP_VERIFY=1`) and visible in the shell history. If GitHub Actions is
  re-enabled, the same `verify` runs there and becomes a required check on
  `main`; the hook stays as the local guard.
- **Proof**: `DistributedStateIntegrationTest` runs against the shared Redis
  every replica would use and asserts that an access change leaves the cache
  untouched before commit and empty after, and that a second runner on another
  thread cannot take a held scheduler lock. `AuditTransactionIntegrationTest`
  proves the audit row commits and rolls back with the write.

## Consequences

- Correctness no longer depends on the number of replicas.
- One Redis round trip per cache miss on permission and provisioning
  lookups; hits are served from Redis in well under a millisecond on the
  same network.
- A new table (`shedlock`) and one migration.
- Rejected alternative: leader election for jobs through Kubernetes leases.
  Correct, but it ties the backend to Kubernetes and does nothing for the
  docker-compose `scale` profile.

## Amendment (2026-09-13, Phase 6): build and image guarantees

- The image is layered (`jarmode=tools extract --layers`), runs on JRE 25 as
  a non-root user and starts from a CDS archive trained at build time with
  the `cds` profile under the same collector as the entrypoint. That
  collector is G1: CDS maps archived heap objects only with compressed oops,
  which ZGC disables, and the measured warm start is faster on G1.
- Every build produces a CycloneDX SBOM (`cyclonedx-maven-plugin`). It is
  inside the jar, at `/app/sbom/admin-api.cdx.json` in the image and served
  by `/actuator/sbom` on the management port.
- `platform` is a library: it declares only the APIs it compiles against,
  never a Boot starter, so `app` alone decides what runs.
- Dependency updates arrive as Dependabot pull requests, which cost no
  Actions minutes. Nothing runs on those branches while CI stays off, so
  `make check` on the branch is the gate before merging.
