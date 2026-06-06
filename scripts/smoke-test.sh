#!/usr/bin/env bash
set -Eeuo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
GATEWAY_BASE_URL="${GATEWAY_BASE_URL:-http://localhost:4000}"
CHECK_GATEWAY="${CHECK_GATEWAY:-true}"

fail() {
  printf 'ERROR: %s\n' "$*" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "$1 is required"
}

require_cmd curl

printf 'Checking API: %s/\n' "$API_BASE_URL"
curl --fail --silent --show-error "$API_BASE_URL/" >/tmp/extreme-gym-api-smoke.json
printf 'API root responded.\n'

if [ "$CHECK_GATEWAY" = "true" ]; then
  printf 'Checking gateway: %s/health\n' "$GATEWAY_BASE_URL"
  curl --fail --silent --show-error "$GATEWAY_BASE_URL/health" >/tmp/extreme-gym-gateway-health-smoke.json
  printf 'Gateway health responded.\n'

  printf 'Checking gateway: %s/status\n' "$GATEWAY_BASE_URL"
  curl --fail --silent --show-error "$GATEWAY_BASE_URL/status" >/tmp/extreme-gym-gateway-status-smoke.json
  printf 'Gateway status responded.\n'
fi

printf 'Basic smoke test passed. Continue with docs/runbooks/smoke-test.md for authenticated flows.\n'
