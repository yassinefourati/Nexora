#!/usr/bin/env bash
# Logical backup of the admin database with pg_dump (custom format, compressed).
#
#   scripts/db-backup.sh                      # uses .env + the compose postgres container
#   DATABASE_URL=postgres://u:p@host:5432/db scripts/db-backup.sh   # any reachable server
#
# Output: backups/admin_db-<UTC timestamp>.dump (kept out of git). Restore with
# scripts/db-restore.sh. Rehearsed on 2026-09-13, see docs/runbooks/backup-restore.md.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${BACKUP_DIR:-$ROOT/backups}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT_DIR"

if [[ -n "${DATABASE_URL:-}" ]]; then
  FILE="$OUT_DIR/admin_db-$STAMP.dump"
  pg_dump --format=custom --compress=6 --no-owner --no-privileges "$DATABASE_URL" > "$FILE"
else
  # shellcheck disable=SC1091
  [[ -f "$ROOT/.env" ]] && set -a && source "$ROOT/.env" && set +a
  : "${POSTGRES_DB:?set POSTGRES_DB or DATABASE_URL}" "${POSTGRES_USER:?}"
  CONTAINER="$(docker compose -f "$ROOT/docker-compose.yml" --profile infra ps -q postgres)"
  [[ -n "$CONTAINER" ]] || { echo "postgres container is not running (docker compose --profile infra up -d)"; exit 1; }
  FILE="$OUT_DIR/admin_db-$STAMP.dump"
  docker exec "$CONTAINER" pg_dump --format=custom --compress=6 --no-owner --no-privileges \
    -U "$POSTGRES_USER" "$POSTGRES_DB" > "$FILE"
fi

SIZE=$(du -h "$FILE" | cut -f1)
echo "backup written: $FILE ($SIZE)"
# Keep the last 14 local backups
ls -1t "$OUT_DIR"/admin_db-*.dump 2>/dev/null | tail -n +15 | xargs -r rm -f
