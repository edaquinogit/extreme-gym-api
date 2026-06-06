#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
ADMIN_LOGIN="${ADMIN_LOGIN:-${APP_BOOTSTRAP_ADMIN_USERNAME:-${ADMIN_USERNAME:-}}}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-${APP_BOOTSTRAP_ADMIN_PASSWORD:-}}"
GATEWAY_BASE_URL="${GATEWAY_BASE_URL:-}"
GATEWAY_ADMIN_API_KEY="${GATEWAY_ADMIN_API_KEY:-}"
SWAGGER_EXPECTED_PUBLIC="${SWAGGER_EXPECTED_PUBLIC:-false}"

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  exit 1
}

info() {
  printf 'OK: %s\n' "$1"
}

warn() {
  printf 'WARN: %s\n' "$1"
}

require_env() {
  local name="$1"
  local value="$2"
  if [[ -z "$value" ]]; then
    fail "$name must be set"
  fi
}

http_status() {
  curl -sS -o /tmp/extreme-smoke-response.json -w '%{http_code}' "$@"
}

json_value() {
  python3 - "$1" /tmp/extreme-smoke-response.json <<'PY'
import json
import sys

key = sys.argv[1]
path = sys.argv[2]
try:
    with open(path, encoding="utf-8") as response_file:
        payload = json.load(response_file)
except json.JSONDecodeError:
    sys.exit(1)

value = payload
for part in key.split("."):
    if not isinstance(value, dict) or part not in value:
        sys.exit(1)
    value = value[part]

if value is None:
    sys.exit(1)
print(value)
PY
}

require_env "ADMIN_LOGIN" "$ADMIN_LOGIN"
require_env "ADMIN_PASSWORD" "$ADMIN_PASSWORD"

status="$(http_status "$API_BASE_URL/")"
[[ "$status" == "200" ]] || fail "API root expected 200, got $status"
info "API root respondeu 200"

login_payload="$(printf '{"username":"%s","password":"%s"}' "$ADMIN_LOGIN" "$ADMIN_PASSWORD")"
status="$(http_status -H 'Content-Type: application/json' -X POST "$API_BASE_URL/auth/login" -d "$login_payload")"
[[ "$status" == "200" ]] || fail "ADMIN login expected 200, got $status"
token="$(json_value token || true)"
[[ -n "$token" ]] || fail "ADMIN login did not return token"
info "ADMIN login emitiu JWT mascarado: ${token:0:12}..."

auth_get() {
  local path="$1"
  local label="$2"
  status="$(http_status -H "Authorization: Bearer $token" "$API_BASE_URL$path")"
  [[ "$status" == "200" ]] || fail "$label expected 200, got $status"
  info "$label respondeu 200"
}

auth_get "/alunos" "Listagem de alunos"
auth_get "/planos" "Listagem de planos"
auth_get "/matriculas" "Listagem de matriculas"
auth_get "/pagamentos" "Listagem de pagamentos"
auth_get "/checkins" "Listagem de check-ins"

device_external_id="smoke-gateway-$(date +%s)"
device_payload="$(printf '{"nome":"Smoke Gateway","tipo":"GATEWAY","modoOperacao":"HIBRIDO","identificadorExterno":"%s"}' "$device_external_id")"
status="$(http_status -H "Authorization: Bearer $token" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso" -d "$device_payload")"
[[ "$status" == "201" ]] || fail "Criacao de dispositivo expected 201, got $status"
device_id="$(json_value dispositivo.id || true)"
device_api_key="$(json_value apiKeyPlaintext || true)"
[[ -n "$device_id" ]] || fail "Criacao de dispositivo nao retornou dispositivo.id"
[[ -n "$device_api_key" ]] || fail "Criacao de dispositivo nao retornou apiKeyPlaintext"
info "Dispositivo tecnico provisionado com API key capturada em memoria"

heartbeat_payload='{"status":"ATIVO","modoOperacao":"HIBRIDO"}'
status="$(http_status -H "X-Device-Api-Key: $device_api_key" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso/$device_id/heartbeat" -d "$heartbeat_payload")"
[[ "$status" == "200" ]] || fail "Heartbeat de dispositivo expected 200, got $status"
info "Heartbeat tecnico respondeu 200"

status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: $device_api_key" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
[[ "$status" == "200" ]] || fail "Snapshot autorizado expected 200, got $status"
info "Snapshot autorizado respondeu 200"

event_key="smoke-event-$(date +%s)"
event_payload="$(printf '{"eventos":[{"idempotencyKey":"%s","resultado":"BLOQUEADO","motivo":"Smoke tecnico sem aluno","modo":"OFFLINE","origem":"DISPOSITIVO","dataHoraEvento":"%s","identificadorExternoEvento":"%s"}]}' "$event_key" "$(date -u +%Y-%m-%dT%H:%M:%S)" "$event_key")"
status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: $device_api_key" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/eventos-acesso/sincronizar-lote" -d "$event_payload")"
[[ "$status" == "200" ]] || fail "Sincronizacao de eventos expected 200, got $status"
info "Sincronizacao de eventos respondeu 200"

status="$(http_status -H "X-Device-Id: $device_id" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
case "$status" in
  401|403)
    info "Snapshot sem API key foi bloqueado com $status"
    ;;
  *)
    fail "Snapshot sem API key deveria responder 401 ou 403; recebeu $status"
    ;;
esac

status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: invalid-smoke-key" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
case "$status" in
  401|403)
    info "Snapshot com API key invalida foi bloqueado com $status"
    ;;
  *)
    fail "Snapshot com API key invalida deveria responder 401 ou 403; recebeu $status"
    ;;
esac

status="$(http_status "$API_BASE_URL/swagger-ui/index.html")"
if [[ "$SWAGGER_EXPECTED_PUBLIC" == "true" ]]; then
  [[ "$status" == "200" ]] || fail "Swagger publico esperado com 200, recebeu $status"
  info "Swagger publico respondeu 200 conforme ambiente local/dev"
else
  case "$status" in
    401|403|404)
      info "Swagger protegido/desabilitado respondeu $status sem JWT"
      ;;
    *)
      fail "Swagger sem JWT deveria responder 401, 403 ou 404; recebeu $status"
      ;;
  esac
fi

if [[ -n "$GATEWAY_BASE_URL" ]]; then
  status="$(http_status "$GATEWAY_BASE_URL/health")"
  [[ "$status" == "200" ]] || fail "Gateway health expected 200, got $status"
  info "Gateway /health respondeu 200"

  status="$(http_status "$GATEWAY_BASE_URL/status")"
  [[ "$status" == "200" ]] || fail "Gateway status expected 200, got $status"
  if grep -Eiq 'api[_-]?key|password|secret|token' /tmp/extreme-smoke-response.json; then
    fail "Gateway /status appears to expose sensitive field names"
  fi
  info "Gateway /status respondeu 200 sem nomes de segredo"

  if [[ -n "$GATEWAY_ADMIN_API_KEY" ]]; then
    status="$(http_status -X POST -H "X-Admin-Api-Key: $GATEWAY_ADMIN_API_KEY" "$GATEWAY_BASE_URL/admin/snapshot-refresh")"
    [[ "$status" == "200" ]] || fail "Gateway snapshot refresh expected 200, got $status"
    info "Gateway snapshot refresh respondeu 200"

    status="$(http_status -X POST -H "X-Admin-Api-Key: $GATEWAY_ADMIN_API_KEY" "$GATEWAY_BASE_URL/admin/heartbeat")"
    [[ "$status" == "200" ]] || fail "Gateway heartbeat expected 200, got $status"
    info "Gateway heartbeat respondeu 200"
  else
    warn "GATEWAY_ADMIN_API_KEY ausente; snapshot/heartbeat via gateway nao executados"
  fi
else
  warn "GATEWAY_BASE_URL ausente; validacoes do processo gateway externo nao executadas"
fi

info "Smoke autenticado concluido incluindo contrato backend de dispositivo"
