#!/usr/bin/env bash
set -Eeuo pipefail

fail() {
  printf 'ERROR: %s\n' "$*" >&2
  exit 1
}

require_var() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    fail "$name must be set"
  fi
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "$1 is required"
}

usage() {
  cat <<'EOF'
Usage:
  RESTORE_TARGET_ENV=staging \
  RESTORE_CONFIRM=RESTORE_STAGING \
  POSTGRES_HOST=localhost \
  POSTGRES_PORT=5432 \
  POSTGRES_DB=extreme_restore \
  POSTGRES_USER=extreme_user \
  POSTGRES_PASSWORD=... \
  scripts/restore-postgres.sh backups/postgres/extreme_db-YYYYMMDD-HHMMSS.sql.gz

Safety:
  - Production restore is refused unless RESTORE_TARGET_ENV=production and
    RESTORE_CONFIRM=RESTORE_PRODUCTION are both set.
  - Staging restore requires RESTORE_CONFIRM=RESTORE_STAGING.
  - The script restores into the configured database. Prefer a disposable
    staging database, never the live database.
EOF
}

BACKUP_FILE="${1:-}"

if [ -z "$BACKUP_FILE" ]; then
  usage
  exit 2
fi

[ -f "$BACKUP_FILE" ] || fail "backup file not found: $BACKUP_FILE"

require_var RESTORE_TARGET_ENV
require_var RESTORE_CONFIRM
require_var POSTGRES_HOST
require_var POSTGRES_DB
require_var POSTGRES_USER
require_var POSTGRES_PASSWORD

POSTGRES_PORT="${POSTGRES_PORT:-5432}"

case "$RESTORE_TARGET_ENV" in
  staging|test|local)
    [ "$RESTORE_CONFIRM" = "RESTORE_STAGING" ] || fail "set RESTORE_CONFIRM=RESTORE_STAGING"
    ;;
  production)
    [ "$RESTORE_CONFIRM" = "RESTORE_PRODUCTION" ] || fail "production restore requires RESTORE_CONFIRM=RESTORE_PRODUCTION"
    ;;
  *)
    fail "RESTORE_TARGET_ENV must be staging, test, local, or production"
    ;;
esac

require_cmd gzip
if [ -n "${POSTGRES_DOCKER_CONTAINER:-}" ]; then
  require_cmd docker
else
  require_cmd psql
fi

scripts/verify-backup.sh "$BACKUP_FILE"

printf 'Restoring %s into %s/%s as %s (%s)\n' "$BACKUP_FILE" "$POSTGRES_HOST" "$POSTGRES_DB" "$POSTGRES_USER" "$RESTORE_TARGET_ENV"
printf 'This must point to a disposable or explicitly approved database.\n'

if [ -n "${POSTGRES_DOCKER_CONTAINER:-}" ]; then
  gzip -dc "$BACKUP_FILE" | docker exec \
    --interactive \
    --env PGPASSWORD="$POSTGRES_PASSWORD" \
    "$POSTGRES_DOCKER_CONTAINER" \
    psql \
      --host "$POSTGRES_HOST" \
      --port "$POSTGRES_PORT" \
      --username "$POSTGRES_USER" \
      --dbname "$POSTGRES_DB" \
      --set ON_ERROR_STOP=on \
      --single-transaction
else
  PGPASSWORD="$POSTGRES_PASSWORD" psql \
    --host "$POSTGRES_HOST" \
    --port "$POSTGRES_PORT" \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --set ON_ERROR_STOP=on \
    --single-transaction \
    --file <(gzip -dc "$BACKUP_FILE")
fi

printf 'Restore completed. Run the restore runbook validation checklist before using this environment.\n'
