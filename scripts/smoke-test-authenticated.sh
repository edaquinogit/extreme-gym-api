#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
ADMIN_LOGIN="${ADMIN_LOGIN:-${APP_BOOTSTRAP_ADMIN_USERNAME:-${ADMIN_USERNAME:-}}}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-${APP_BOOTSTRAP_ADMIN_PASSWORD:-}}"
DEVICE_NAME="${DEVICE_NAME:-Smoke Gateway}"
DEVICE_TYPE="${DEVICE_TYPE:-GATEWAY}"
DEVICE_MODE="${DEVICE_MODE:-HIBRIDO}"
SMOKE_CREATE_TEST_DATA="${SMOKE_CREATE_TEST_DATA:-false}"
GATEWAY_BASE_URL="${GATEWAY_BASE_URL:-}"
GATEWAY_ADMIN_API_KEY="${GATEWAY_ADMIN_API_KEY:-}"
SWAGGER_EXPECTED_PUBLIC="${SWAGGER_EXPECTED_PUBLIC:-false}"

RESPONSE_FILE="$(mktemp /tmp/extreme-smoke-response.XXXXXX.json)"
trap 'rm -f "$RESPONSE_FILE"' EXIT

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
  curl -sS -o "$RESPONSE_FILE" -w '%{http_code}' "$@"
}

json_value() {
  python3 - "$1" "$RESPONSE_FILE" <<'PY'
import json
import sys

query = sys.argv[1]
path = sys.argv[2]

try:
    with open(path, encoding="utf-8") as response_file:
        payload = json.load(response_file)
except json.JSONDecodeError:
    sys.exit(1)

value = payload
for part in query.split("."):
    if isinstance(value, list):
        try:
            value = value[int(part)]
        except (ValueError, IndexError):
            sys.exit(1)
        continue
    if not isinstance(value, dict) or part not in value:
        sys.exit(1)
    value = value[part]

if value is None:
    sys.exit(1)
if isinstance(value, bool):
    print(str(value).lower())
else:
    print(value)
PY
}

json_has_forbidden_key() {
  python3 - "$RESPONSE_FILE" "$@" <<'PY'
import json
import sys

path = sys.argv[1]
forbidden = {key.lower() for key in sys.argv[2:]}

try:
    with open(path, encoding="utf-8") as response_file:
        payload = json.load(response_file)
except json.JSONDecodeError:
    sys.exit(2)

def walk(value):
    if isinstance(value, dict):
        for key, item in value.items():
            if key.lower() in forbidden:
                return True
            if walk(item):
                return True
    if isinstance(value, list):
        return any(walk(item) for item in value)
    return False

sys.exit(0 if walk(payload) else 1)
PY
}

assert_status() {
  local expected="$1"
  local actual="$2"
  local label="$3"
  [[ "$actual" == "$expected" ]] || fail "$label expected $expected, got $actual"
  info "$label respondeu $actual"
}

assert_rejected() {
  local actual="$1"
  local label="$2"
  case "$actual" in
    401|403)
      info "$label foi bloqueado com $actual"
      ;;
    *)
      fail "$label deveria responder 401 ou 403; recebeu $actual"
      ;;
  esac
}

auth_json() {
  local method="$1"
  local path="$2"
  local payload="$3"
  http_status -H "Authorization: Bearer $token" -H 'Content-Type: application/json' -X "$method" "$API_BASE_URL$path" -d "$payload"
}

auth_get() {
  local path="$1"
  local label="$2"
  status="$(http_status -H "Authorization: Bearer $token" "$API_BASE_URL$path")"
  assert_status "200" "$status" "$label"
}

device_json() {
  local method="$1"
  local path="$2"
  local payload="$3"
  http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: $device_api_key" -H 'Content-Type: application/json' -X "$method" "$API_BASE_URL$path" -d "$payload"
}

now_local() {
  date -u +%Y-%m-%dT%H:%M:%S
}

require_env "ADMIN_LOGIN" "$ADMIN_LOGIN"
require_env "ADMIN_PASSWORD" "$ADMIN_PASSWORD"

status="$(http_status "$API_BASE_URL/")"
assert_status "200" "$status" "API root"

login_payload="$(printf '{"username":"%s","password":"%s"}' "$ADMIN_LOGIN" "$ADMIN_PASSWORD")"
status="$(http_status -H 'Content-Type: application/json' -X POST "$API_BASE_URL/auth/login" -d "$login_payload")"
assert_status "200" "$status" "ADMIN login"
token="$(json_value token || true)"
[[ -n "$token" ]] || fail "ADMIN login did not return token"
info "ADMIN login emitiu JWT mascarado: ${token:0:12}..."

auth_get "/alunos" "Listagem de alunos"
auth_get "/planos" "Listagem de planos"
auth_get "/matriculas" "Listagem de matriculas"
auth_get "/pagamentos" "Listagem de pagamentos"
auth_get "/checkins" "Listagem de check-ins"

device_external_id="smoke-gateway-$(date +%s)"
device_payload="$(printf '{"nome":"%s","tipo":"%s","modoOperacao":"%s","identificadorExterno":"%s"}' "$DEVICE_NAME" "$DEVICE_TYPE" "$DEVICE_MODE" "$device_external_id")"
status="$(auth_json POST "/dispositivos-acesso" "$device_payload")"
assert_status "201" "$status" "Criacao de dispositivo"
device_id="$(json_value dispositivo.id || true)"
device_api_key="$(json_value apiKeyPlaintext || true)"
[[ -n "$device_id" ]] || fail "Criacao de dispositivo nao retornou dispositivo.id"
[[ -n "$device_api_key" ]] || fail "Criacao de dispositivo nao retornou apiKeyPlaintext"
info "Dispositivo tecnico provisionado com API key capturada somente em memoria"

auth_get "/dispositivos-acesso" "Listagem de dispositivos"
if json_has_forbidden_key apiKeyPlaintext apiKeyHash chaveApiPlaintext chaveApiHash hash; then
  fail "Listagem de dispositivos expos campo sensivel de API key"
fi
info "Listagem de dispositivos nao expos API key nem hash"

heartbeat_payload="$(printf '{"status":"ATIVO","modoOperacao":"%s"}' "$DEVICE_MODE")"
status="$(http_status -H "X-Device-Api-Key: $device_api_key" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso/$device_id/heartbeat" -d "$heartbeat_payload")"
assert_status "200" "$status" "Heartbeat tecnico"
last_seen="$(json_value ultimaComunicacaoEm || true)"
[[ -n "$last_seen" ]] || fail "Heartbeat nao retornou ultimaComunicacaoEm"

status="$(http_status -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso/$device_id/heartbeat" -d "$heartbeat_payload")"
assert_rejected "$status" "Heartbeat sem API key"

status="$(http_status -H "X-Device-Api-Key: invalid-smoke-key" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso/$device_id/heartbeat" -d "$heartbeat_payload")"
assert_rejected "$status" "Heartbeat com API key invalida"

status="$(http_status -H "Authorization: Bearer $token" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/dispositivos-acesso/$device_id/heartbeat" -d "$heartbeat_payload")"
assert_rejected "$status" "Heartbeat com JWT humano sem API key tecnica"

status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: $device_api_key" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
assert_status "200" "$status" "Snapshot autorizado"
if json_has_forbidden_key email telefone cpf documento endereco imagemFacial templateBiometrico dadosFinanceiros; then
  fail "Snapshot expos campo de PII ou biometria fora do contrato"
fi
info "Snapshot nao expos campos de PII/biometria proibidos"

status="$(http_status -H "X-Device-Id: $device_id" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
assert_rejected "$status" "Snapshot sem API key"

status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: invalid-smoke-key" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
assert_rejected "$status" "Snapshot com API key invalida"

status="$(http_status -H "Authorization: Bearer $token" -H "X-Device-Id: $device_id" "$API_BASE_URL/controle-acesso/snapshot-autorizados")"
assert_rejected "$status" "Snapshot com JWT humano sem API key tecnica"

missing_credential="smoke-missing-$(date +%s)"
missing_validation_payload="$(printf '{"credencialTipo":"QR_CODE","identificadorExterno":"%s","origem":"DISPOSITIVO","idempotencyKey":"%s","dataHoraEvento":"%s"}' "$missing_credential" "smoke-validate-missing-$missing_credential" "$(now_local)")"
status="$(device_json POST "/controle-acesso/validar-dispositivo" "$missing_validation_payload")"
assert_status "200" "$status" "Validacao de credencial inexistente"
missing_allowed="$(json_value permitido || true)"
missing_result="$(json_value resultado || true)"
missing_event_id="$(json_value eventoId || true)"
[[ "$missing_allowed" == "false" ]] || fail "Credencial inexistente deveria retornar permitido=false"
[[ "$missing_result" == "BLOQUEADO" ]] || fail "Credencial inexistente deveria retornar BLOQUEADO"
[[ -n "$missing_event_id" ]] || fail "Credencial inexistente nao gerou evento"
info "Credencial inexistente bloqueou e gerou evento"

event_base="smoke-event-$(date +%s)"
event_time="$(now_local)"
event_payload="$(printf '{"eventos":[{"idempotencyKey":"%s-1","resultado":"LIBERADO","motivo":"Smoke liberado offline","modo":"OFFLINE","origem":"DISPOSITIVO","dataHoraEvento":"%s","identificadorExternoEvento":"%s-1"},{"idempotencyKey":"%s-2","resultado":"BLOQUEADO","motivo":"Smoke bloqueado offline","modo":"OFFLINE","origem":"DISPOSITIVO","dataHoraEvento":"%s","identificadorExternoEvento":"%s-2"},{"idempotencyKey":"%s-2","resultado":"BLOQUEADO","motivo":"Smoke duplicado offline","modo":"OFFLINE","origem":"DISPOSITIVO","dataHoraEvento":"%s","identificadorExternoEvento":"%s-2-dup"}]}' "$event_base" "$event_time" "$event_base" "$event_base" "$event_time" "$event_base" "$event_base" "$event_time" "$event_base")"
status="$(device_json POST "/eventos-acesso/sincronizar-lote" "$event_payload")"
assert_status "200" "$status" "Sincronizacao de eventos"
total_recebidos="$(json_value totalRecebidos || true)"
total_criados="$(json_value totalCriados || true)"
total_duplicados="$(json_value totalDuplicados || true)"
total_rejeitados="$(json_value totalRejeitados || true)"
[[ "$total_recebidos" == "3" ]] || fail "Sync lote deveria receber 3 eventos; recebeu $total_recebidos"
[[ "$total_criados" == "2" ]] || fail "Sync lote deveria criar 2 eventos; criou $total_criados"
[[ "$total_duplicados" == "1" ]] || fail "Sync lote deveria duplicar 1 evento; duplicou $total_duplicados"
[[ "$total_rejeitados" == "0" ]] || fail "Sync lote deveria rejeitar 0 eventos; rejeitou $total_rejeitados"
info "Sync lote validou contadores de criados/duplicados/rejeitados"

status="$(http_status -H "X-Device-Id: $device_id" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/eventos-acesso/sincronizar-lote" -d "$event_payload")"
assert_rejected "$status" "Sync lote sem API key"

status="$(http_status -H "X-Device-Id: $device_id" -H "X-Device-Api-Key: invalid-smoke-key" -H 'Content-Type: application/json' -X POST "$API_BASE_URL/eventos-acesso/sincronizar-lote" -d "$event_payload")"
assert_rejected "$status" "Sync lote com API key invalida"

if [[ "$SMOKE_CREATE_TEST_DATA" == "true" ]]; then
  unique="$(date +%s)"
  aluno_payload="$(printf '{"nome":"Smoke Runtime Aluno %s","email":"smoke.%s@example.com","telefone":"7199999%s"}' "$unique" "$unique" "${unique: -4}")"
  status="$(auth_json POST "/alunos" "$aluno_payload")"
  assert_status "201" "$status" "Criacao de aluno smoke"
  aluno_id="$(json_value id || true)"
  [[ -n "$aluno_id" ]] || fail "Criacao de aluno smoke nao retornou id"

  plano_payload="$(printf '{"nome":"Smoke Plano %s","descricao":"Plano temporario para smoke runtime","valorMensal":99.90,"duracaoEmDias":30}' "$unique")"
  status="$(auth_json POST "/planos" "$plano_payload")"
  assert_status "201" "$status" "Criacao de plano smoke"
  plano_id="$(json_value id || true)"
  [[ -n "$plano_id" ]] || fail "Criacao de plano smoke nao retornou id"

  matricula_payload="$(printf '{"alunoId":%s,"planoId":%s,"dataInicio":"%s"}' "$aluno_id" "$plano_id" "$(date -u +%Y-%m-%d)")"
  status="$(auth_json POST "/matriculas" "$matricula_payload")"
  assert_status "201" "$status" "Criacao de matricula smoke"
  matricula_id="$(json_value id || true)"
  [[ -n "$matricula_id" ]] || fail "Criacao de matricula smoke nao retornou id"

  pagamento_payload="$(printf '{"matriculaId":%s,"valor":99.90,"formaPagamento":"PIX"}' "$matricula_id")"
  status="$(auth_json POST "/pagamentos" "$pagamento_payload")"
  assert_status "201" "$status" "Registro de pagamento smoke"

  credential_external_id="smoke-qr-$unique"
  credential_payload="$(printf '{"tipo":"QR_CODE","identificadorExterno":"%s","fornecedor":"smoke-runtime","termoAceitoEm":"%s","versaoTermo":"smoke-v1"}' "$credential_external_id" "$(now_local)")"
  status="$(auth_json POST "/alunos/$aluno_id/credenciais-acesso" "$credential_payload")"
  assert_status "201" "$status" "Criacao de credencial smoke"
  credential_id="$(json_value id || true)"
  [[ -n "$credential_id" ]] || fail "Criacao de credencial smoke nao retornou id"

  allowed_validation_payload="$(printf '{"credencialTipo":"QR_CODE","identificadorExterno":"%s","origem":"DISPOSITIVO","idempotencyKey":"smoke-validate-allowed-%s","dataHoraEvento":"%s"}' "$credential_external_id" "$unique" "$(now_local)")"
  status="$(device_json POST "/controle-acesso/validar-dispositivo" "$allowed_validation_payload")"
  assert_status "200" "$status" "Validacao de credencial ativa"
  allowed="$(json_value permitido || true)"
  result="$(json_value resultado || true)"
  event_id="$(json_value eventoId || true)"
  [[ "$allowed" == "true" ]] || fail "Credencial ativa deveria retornar permitido=true"
  [[ "$result" == "LIBERADO" ]] || fail "Credencial ativa deveria retornar LIBERADO"
  [[ -n "$event_id" ]] || fail "Credencial ativa nao gerou evento"
  info "Credencial ativa liberou acesso e gerou evento"

  status="$(auth_json PATCH "/credenciais-acesso/$credential_id/revogar" '{}')"
  assert_status "200" "$status" "Revogacao de credencial smoke"

  revoked_validation_payload="$(printf '{"credencialTipo":"QR_CODE","identificadorExterno":"%s","origem":"DISPOSITIVO","idempotencyKey":"smoke-validate-revoked-%s","dataHoraEvento":"%s"}' "$credential_external_id" "$unique" "$(now_local)")"
  status="$(device_json POST "/controle-acesso/validar-dispositivo" "$revoked_validation_payload")"
  assert_status "200" "$status" "Validacao de credencial revogada"
  revoked_allowed="$(json_value permitido || true)"
  revoked_result="$(json_value resultado || true)"
  revoked_event_id="$(json_value eventoId || true)"
  [[ "$revoked_allowed" == "false" ]] || fail "Credencial revogada deveria retornar permitido=false"
  [[ "$revoked_result" == "BLOQUEADO" ]] || fail "Credencial revogada deveria retornar BLOQUEADO"
  [[ -n "$revoked_event_id" ]] || fail "Credencial revogada nao gerou evento"
  info "Credencial revogada bloqueou acesso e gerou evento"
else
  warn "SMOKE_CREATE_TEST_DATA=false; validacao de credencial ativa/revogada com massa criada pelo smoke nao executada"
fi

status="$(http_status "$API_BASE_URL/swagger-ui/index.html")"
if [[ "$SWAGGER_EXPECTED_PUBLIC" == "true" ]]; then
  assert_status "200" "$status" "Swagger publico"
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
  assert_status "200" "$status" "Gateway /health"

  status="$(http_status "$GATEWAY_BASE_URL/status")"
  assert_status "200" "$status" "Gateway /status"
  if grep -Eiq 'api[_-]?key|password|secret|token' "$RESPONSE_FILE"; then
    fail "Gateway /status appears to expose sensitive field names"
  fi
  info "Gateway /status nao exibiu nomes de segredo"

  if [[ -n "$GATEWAY_ADMIN_API_KEY" ]]; then
    status="$(http_status -X POST -H "X-Admin-Api-Key: $GATEWAY_ADMIN_API_KEY" "$GATEWAY_BASE_URL/admin/snapshot-refresh")"
    assert_status "200" "$status" "Gateway snapshot refresh"

    status="$(http_status -X POST -H "X-Admin-Api-Key: $GATEWAY_ADMIN_API_KEY" "$GATEWAY_BASE_URL/admin/heartbeat")"
    assert_status "200" "$status" "Gateway heartbeat"
  else
    warn "GATEWAY_ADMIN_API_KEY ausente; snapshot/heartbeat via gateway nao executados"
  fi
else
  warn "GATEWAY_BASE_URL ausente; validacoes do processo gateway externo nao executadas"
fi

info "Smoke autenticado concluido incluindo contrato backend de dispositivo"
