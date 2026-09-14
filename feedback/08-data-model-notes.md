# Data model notes

## No optimistic locking

Covered in `03-architecture-and-domain-logic.md` too, repeating here since
it's fundamentally a schema decision: no entity has a `version` column, so
concurrent updates silently last-write-wins with no conflict detection. Easy
migration (`ALTER TABLE ... ADD COLUMN version BIGINT NOT NULL DEFAULT 0` on
the mutable tables), plus `@Version` on `BaseEntity` and a 409 mapping in
`GlobalExceptionHandler`.

## Materialized view never refreshed

`user_permissions_mv` — see `04-scaling-and-resilience.md` for detail.
Either schedule the refresh or remove the view; right now it's silently
stale forever, which is worse than not having it, because it *looks* like a
performance optimization that's actually serving wrong data.

## Double audit rows

`fn_audit_trigger()` (DB trigger, 6 tables) and `AuditAspect`
(`@Audited`-annotated service methods) both write to `audit_logs`
independently, for the same logical write. See
`02-security-followups.md` item 5 for the recommended fix (pick one
mechanism, most likely wiring `app.current_user_id` into the trigger path
since it captures a real before/after diff automatically).

## `ip_address` columns are `VARCHAR(45)`, not `inet`

`V14__ip_address_as_varchar.sql` changed `audit_logs`, `auth_logs`,
`sessions`, and `login_history`'s `ip_address` columns from Postgres's
native `inet` type to plain varchar, because Hibernate couldn't bind a
`String` parameter against an `inet` column without extra driver glue. This
is a reasonable pragmatic call for an app that (per the migration's own
comment) never does subnet-aware queries — just flagging it so it's a
documented trade-off: if you ever *do* want "find all logins from this
subnet" type queries, you'd need to either add the JDBC binding support
Hibernate was missing, or do subnet matching in application code instead of
SQL.

## `RateLimit` entity and repository are unused

`persistence/.../domain/RateLimit.java` and
`persistence/.../repository/RateLimitRepository.java` exist, backed by a
real table (`rate_limits`, created in `V5__audit_security_logs.sql`). The
actual rate limiting (`RateLimitFilter`) is entirely in-memory and never
touches this entity or table. If you ever move rate
limiting to be DB-backed (for multi-replica correctness — see
`04-scaling-and-resilience.md`), this table might be the intended home for
it, or it might be leftover scaffolding. Worth checking whether the table
was meant for something else (rate-limit *audit trail*, maybe, distinct from
the live enforcement mechanism) before deciding to keep, repurpose, or drop
it.

## Soft-delete is inconsistent across the schema

`SoftDeletableEntity`'s javadoc says it's for "users, roles, organizations,
departments, teams, menus, menu_items, tags" — 8 of 33 tables. Everything
else (comments, attachments, audit logs, sessions, notifications, etc.) is
hard-deleted. That's a defensible product decision (you don't soft-delete an
audit log — it's already immutable history; you might not need to
soft-delete a session either), but worth a deliberate second look at
`comments` and `attachments` specifically: if a user's comment on something
gets hard-deleted and later needs to be recovered (moderation dispute,
accidental delete), there's no way to get it back today. Not flagging as a
bug, just as a decision worth confirming was made on purpose rather than by
omission.
