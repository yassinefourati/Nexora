#!/usr/bin/env bash
# Restore a pg_dump custom-format backup produced by scripts/db-backup.sh.
#
#   scripts/db-restore.sh backups/admin_db-20260913T120000Z.dump            # into the compose postgres
#   DATABASE_URL=postgres://u:p@host:5432/db scripts/db-restore.sh FILE      # into any server
#
# The target database is dropped and recreated: this is a full replace, not a
# merge. Flyway's schema_history is part of the dump, so the application starts
# cleanly against the restored state without re-running migrations.
set -euo pipefail

FILE="${1:?usage: db-restore.sh <backup.dump>}"
[[ -f "$FILE" ]] || { echo "no such file: $FILE"; exit 1; }
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
START=$(date +%s)

if [[ -n "${DATABASE_URL:-}" ]]; then
  DB="${DATABASE_URL##*/}"; DB="${DB%%\?*}"
  ADMIN_URL="${DATABASE_URL%/*}/postgres"
  psql "$ADMIN_URL" -v ON_ERROR_STOP=1 -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB' AND pid <> pg_backend_pid();" > /dev/null
  psql "$ADMIN_URL" -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS \"$DB\";" -c "CREATE DATABASE \"$DB\";"
  pg_restore --no-owner --no-privileges --dbname="$DATABASE_URL" "$FILE"
else
  # shellcheck disable=SC1091
  [[ -f "$ROOT/.env" ]] && set -a && source "$ROOT/.env" && set +a
  : "${POSTGRES_DB:?set POSTGRES_DB or DATABASE_URL}" "${POSTGRES_USER:?}"
  CONTAINER="$(docker compose -f "$ROOT/docker-compose.yml" --profile infra ps -q postgres)"
  [[ -n "$CONTAINER" ]] || { echo "postgres container is not running"; exit 1; }
  docker exec "$CONTAINER" psql -U "$POSTGRES_USER" -d postgres -v ON_ERROR_STOP=1 \
    -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$POSTGRES_DB' AND pid <> pg_backend_pid();" > /dev/null
  docker exec "$CONTAINER" psql -U "$POSTGRES_USER" -d postgres -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE IF EXISTS \"$POSTGRES_DB\";" -c "CREATE DATABASE \"$POSTGRES_DB\";"
  docker exec -i "$CONTAINER" pg_restore --no-owner --no-privileges -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$FILE"
fi

echo "restore complete in $(( $(date +%s) - START ))s from $FILE"
