#!/usr/bin/env bash
set -Eeuo pipefail

timestamp() {
  date +"%Y-%m-%dT%H:%M:%S%z"
}

log() {
  printf '[%s] %s\n' "$(timestamp)" "$*"
}

fail() {
  log "ERROR: $*"
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

require_var POSTGRES_HOST
require_var POSTGRES_DB
require_var POSTGRES_USER
require_var POSTGRES_PASSWORD

POSTGRES_PORT="${POSTGRES_PORT:-5432}"
BACKUP_DIR="${BACKUP_DIR:-./backups/postgres}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"
LOG_DIR="${BACKUP_LOG_DIR:-./logs/backups}"

case "$BACKUP_RETENTION_DAYS" in
  ''|*[!0-9]*) fail "BACKUP_RETENTION_DAYS must be numeric" ;;
esac

if [ "$BACKUP_RETENTION_DAYS" -lt 7 ] || [ "$BACKUP_RETENTION_DAYS" -gt 30 ]; then
  fail "BACKUP_RETENTION_DAYS must be between 7 and 30"
fi

require_cmd gzip
require_cmd find

if [ -n "${POSTGRES_DOCKER_CONTAINER:-}" ]; then
  require_cmd docker
else
  require_cmd pg_dump
fi

mkdir -p "$BACKUP_DIR" "$LOG_DIR"

STAMP="$(date +"%Y%m%d-%H%M%S")"
BASE_NAME="${POSTGRES_DB}-${STAMP}.sql.gz"
BACKUP_PATH="${BACKUP_DIR}/${BASE_NAME}"
LOG_PATH="${LOG_DIR}/backup-${STAMP}.log"

{
  log "Starting PostgreSQL backup for database '$POSTGRES_DB' on host '$POSTGRES_HOST:$POSTGRES_PORT'"
  umask 077
  if [ -n "${POSTGRES_DOCKER_CONTAINER:-}" ]; then
    docker exec \
      --env PGPASSWORD="$POSTGRES_PASSWORD" \
      "$POSTGRES_DOCKER_CONTAINER" \
      pg_dump \
        --host "$POSTGRES_HOST" \
        --port "$POSTGRES_PORT" \
        --username "$POSTGRES_USER" \
        --dbname "$POSTGRES_DB" \
        --format plain \
        --no-owner \
        --no-privileges \
      | gzip -9 > "$BACKUP_PATH"
  else
    PGPASSWORD="$POSTGRES_PASSWORD" pg_dump \
      --host "$POSTGRES_HOST" \
      --port "$POSTGRES_PORT" \
      --username "$POSTGRES_USER" \
      --dbname "$POSTGRES_DB" \
      --format plain \
      --no-owner \
      --no-privileges \
      | gzip -9 > "$BACKUP_PATH"
  fi

  test -s "$BACKUP_PATH" || fail "backup file was not created or is empty: $BACKUP_PATH"

  SHA_PATH="${BACKUP_PATH}.sha256"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$BACKUP_PATH" > "$SHA_PATH"
    log "Checksum written to $SHA_PATH"
  else
    log "sha256sum not found; checksum skipped"
  fi

  find "$BACKUP_DIR" -type f \( -name "${POSTGRES_DB}-*.sql.gz" -o -name "${POSTGRES_DB}-*.sql.gz.sha256" \) -mtime +"$BACKUP_RETENTION_DAYS" -print -delete

  log "Backup completed: $BACKUP_PATH"
} 2>&1 | tee "$LOG_PATH"
