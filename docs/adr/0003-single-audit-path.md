# ADR 0003: One audit path, written after commit, keyed on the identity subject

Status: accepted (2026-09-13); amended by [ADR 0007](0007-audit-in-transaction-and-durable-events.md): the audit row is now written inside the business transaction.

## Context

Two mechanisms wrote `audit_logs`: a PL/pgSQL trigger on six tables (V13,
`fn_audit_trigger`) and the application-level `AuditAspect` +
`AuditLogEventListener` pair. The trigger read `app.current_user_id`, a session
variable the application never set, so every trigger row had a null actor. The
listener ran synchronously in a `REQUIRES_NEW` transaction, so an audit row
could be committed for a business transaction that later rolled back. Each
audited write therefore produced two rows, one of them anonymous and one of
them possibly false.

## Decision

- The application path is the only audit path. V16 drops the six triggers; the
  function is kept in case an actor-aware trigger is wanted later.
- `AuditLogEventListener` is a `@TransactionalEventListener(AFTER_COMMIT)` with
  `fallbackExecution` for calls outside a transaction. An audit row exists only
  for writes that happened.
- `AuditEvent` carries both the human-readable actor and the identity-provider
  subject. The listener resolves the actor by subject through
  `user_identity_providers`, falling back to username, and never fails the
  write when the actor is unknown.
- `audit_logs` stays range-partitioned by month. `AuditLogPartitionJob` creates
  partitions ahead of time and drops those older than
  `app.audit.retention-months` (default 24), which is O(1) per month instead of
  a DELETE over millions of rows.
- `GET /audit-logs/export` provides CSV, PDF and JSON exports capped at
  `app.export.max-rows`.

## Consequences

- Before/after JSON diffs, which only the trigger produced, are no longer
  captured. If they become a requirement, the right fix is to set
  `app.current_user_id` per transaction and re-attach the trigger, not to run
  both paths.
- Writes that bypass the service layer (a raw repository call without
  `@Audited`) are not audited. ArchUnit forbids controllers from calling
  repositories, so the only such writes are internal jobs.
