# Runbook: database backup and restore

## What is backed up

The whole `admin_db` database with `pg_dump --format=custom --compress=6`:
schema, data, the partitioned `audit_logs` with every attached partition, and
`flyway_schema_history`. Because the migration history is part of the dump,
the application starts cleanly against a restored database without re-running
or re-validating migrations.

## Scripts

| Script | Does |
|---|---|
| `scripts/db-backup.sh` | Writes `backups/admin_db-<UTC>.dump`, keeps the last 14 locally. Uses the compose Postgres from `.env`, or any server via `DATABASE_URL`. |
| `scripts/db-restore.sh FILE` | Terminates connections, drops and recreates the database, `pg_restore`s the file. A full replace, not a merge. |

Both work against the compose Postgres (`--profile infra`/`app`) and against a
managed server (`DATABASE_URL=postgres://user:pass@host:5432/admin_db`).

## Rehearsal record

Run on 2026-09-13 against the compose Postgres 17 with the seeded dataset
plus the smoke-test rows (2 users, 10 audit rows, 16 Flyway migrations):

| Step | Result |
|---|---|
| Backup | 1 s, 156 KB dump |
| Canary row inserted after the backup | present |
| Restore (drop, create, `pg_restore`) | 1 s |
| Canary after restore | gone (restore replaced the database) |
| `flyway_schema_history` rows after restore | 16 |
| Running API after restore (`/actuator/health`) | 200, no restart needed; Hikari reconnected |

Timings scale with data volume; re-run the rehearsal and update this table
whenever the database grows by an order of magnitude or the Postgres major
version changes.

## Production guidance

- Schedule `db-backup.sh` (or the managed provider's snapshots) at least daily;
  ship dumps off the database host. `audit_logs` partitions older than
  `app.audit.retention-months` are dropped by the API, so a dump is also the
  only long-term audit archive.
- Restore into a **fresh** database first (`DATABASE_URL` pointing at a scratch
  server) and boot a single API replica against it to validate, before ever
  restoring over production.
- The API tolerates a restore under it (connections are re-established), but
  in-flight writes during the drop window fail with 500s. Announce a short
  maintenance window or scale the API to zero first.
- After a restore, Redis idempotency keys and rate-limit buckets are stale
  relative to the database; flushing Redis (`redis-cli FLUSHDB`) is safe and
  recommended.
