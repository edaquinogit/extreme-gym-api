#!/usr/bin/env bash
set -Eeuo pipefail

fail() {
  printf 'ERROR: %s\n' "$*" >&2
  exit 1
}

BACKUP_FILE="${1:-}"

if [ -z "$BACKUP_FILE" ]; then
  fail "usage: scripts/verify-backup.sh path/to/backup.sql.gz"
fi

if [ ! -f "$BACKUP_FILE" ]; then
  fail "backup file not found: $BACKUP_FILE"
fi

if [ ! -s "$BACKUP_FILE" ]; then
  fail "backup file is empty: $BACKUP_FILE"
fi

command -v gzip >/dev/null 2>&1 || fail "gzip is required"

gzip -t "$BACKUP_FILE"

if [ -f "${BACKUP_FILE}.sha256" ]; then
  command -v sha256sum >/dev/null 2>&1 || fail "sha256sum is required to verify checksum"
  sha256sum -c "${BACKUP_FILE}.sha256"
else
  printf 'WARN: checksum file not found: %s.sha256\n' "$BACKUP_FILE"
fi

printf 'Backup looks readable: %s\n' "$BACKUP_FILE"
