# Local Stack Credentials

Seeded by `backend/keycloak/project-realm.json` (Keycloak users) and
`backend/src/main/resources/db/migration/V9__seed_admin_backup_data.sql`
(backend `users` table row). All are dev/demo-only credentials — never reuse
these values outside a local Docker environment.

## Admin Panel (frontend) — sign in at http://localhost:5173

| Username     | Password       | Keycloak realm role | Backend `users` row |
|--------------|----------------|----------------------|----------------------|
| `superadmin` | `ChangeMe123!` | `admin`              | Yes — full app access |
| `manager1`   | `ChangeMe123!` | `manager`            | No — Keycloak-only |
| `user1`      | `ChangeMe123!` | `user`               | No — Keycloak-only |

Only `superadmin` has a matching row in the backend's `users` table, so it's
the only account that will resolve correctly everywhere the app looks up
"the current user's backend record" (Profile, notifications, audit actor,
etc.). Use `manager1`/`user1` only to test role-based permission differences
where a full backend identity isn't required.

## Keycloak admin console — http://localhost:9090

| Username | Password |
|----------|----------|
| `admin`  | `admin`  |

Manage realms/users/clients directly at http://localhost:9090/admin.

## Postgres — localhost:5433

| Field    | Value         |
|----------|---------------|
| Database | `admin_db`    |
| Username | `admin_backend` |
| Password | `secret`      |

Connect with: `psql -h localhost -p 5433 -U admin_backend -d admin_db`

## Configuration

All secrets above are read from `.env` at the project root (copy `.env.example`
to `.env` to customize). `RATE_LIMIT_RPM` in `.env` controls the backend's
per-IP request limit — defaults to 60 (production-accurate); raise it locally
if concurrent testing/demo use hits 429s.
