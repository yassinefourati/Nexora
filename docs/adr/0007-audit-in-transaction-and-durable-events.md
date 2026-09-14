# ADR 0007: Audit rows are written in the business transaction; integration events are durable

Status: accepted (2026-09-13). Amends ADR 0003.

## Context

ADR 0003 made the application path the only audit path and moved the
listener to `AFTER_COMMIT` so a rolled-back write never leaves an audit row.
To avoid holding a second pooled connection per write, the listener was then
made `@Async` with `REQUIRES_NEW`. The result is at-most-once delivery: a
crash, a pod eviction or an executor rejection between the commit and the
asynchronous insert loses the audit row silently. For a compliance record
that is the wrong trade.

The same fire-and-forget pattern is used for the WebSocket push after
commit and, in the target architecture, for cross-replica cache
invalidation.

## Decision

- The audit row is written **inside the business transaction** by a
  synchronous `@EventListener` on the domain event. The audit table lives in
  the same database, so atomicity is free: the write and its audit row
  commit or roll back together. No `@Async`, no `REQUIRES_NEW`, no second
  connection.
- The `audit_logs_default` partition stays in place so an insert can never
  fail for lack of a partition. `AuditLogPartitionJob` keeps creating the
  monthly partitions ahead of time under a scheduler lock (ADR 0008).
- Events that must cross a module or a replica boundary after commit
  (`UserNotificationAssigned`, `AccessChanged`, `UserAccessRevoked`) go
  through the **Spring Modulith event publication registry**: they are
  persisted in the same transaction and delivered after commit, with
  incomplete publications replayed on restart. Delivery is at-least-once;
  listeners are idempotent.
- The `[AUDIT]` log line produced by `AuditAspect` is kept for failed
  attempts, which have no committed write and therefore no audit row.

## Consequences

- An audit row exists for exactly the writes that happened, with no window
  in which it can be lost.
- Each write transaction carries one extra insert. Measured cost is
  negligible against the round trip already paid.
- The connection-pool exhaustion observed in ADR 0003 cannot recur because
  no second transaction is opened.
- Rejected alternative: a hand-written outbox table. The Modulith registry
  is the same pattern with the polling, replay and completion bookkeeping
  already implemented.
