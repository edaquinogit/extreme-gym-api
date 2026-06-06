# Phase 1 Pilot Go/No-Go Report

Data/hora da validacao: 2026-06-06 15:54:11 -03.

Escopo: piloto controlado em staging operacional. Nao houve liberacao de producao, teste de hardware real ou Face ID.

## Decisao

NO-GO para iniciar piloto operacional completo neste momento.

O ambiente de staging, backup, restore e smoke basico estao funcionais. A decisao permanece NO-GO porque dois pre-requisitos de piloto real ainda falham:

- nao existe usuario ADMIN/operacional provisionado no banco de staging em profile `prod`;
- gateway consegue iniciar e expor health/status, mas snapshot e heartbeat retornam 401 porque a credencial/dispositivo ainda nao esta provisionada/autorizada no backend.

Quando esses dois pontos forem resolvidos e retestados, a decisao pode evoluir para GO condicionado.

## Diagnostico Inicial

- Staging foi iniciado com Docker Compose usando profile Spring `prod`.
- PostgreSQL real `postgres:16` foi usado.
- Banco da API nao ficou publicado em porta externa; apenas o app foi exposto em `18080`.
- Swagger em profile `prod` retornou `401`, comportamento esperado para ambiente protegido.
- Logs confirmaram Flyway com 3 migrations aplicadas ate `v3`.
- `pg_dump` e `psql` nao estao instalados no host; os scripts foram adaptados para modo container via `POSTGRES_DOCKER_CONTAINER`.

## Staging Validado

- Containers `extreme_phase1-app-1` e `extreme_phase1-postgres-1` ficaram `healthy`.
- API respondeu `200` em `GET /`.
- App iniciou em profile `prod`.
- Flyway aplicou:
  - `V1__create_initial_schema.sql`
  - `V2__set_default_status_aluno.sql`
  - `V3__create_access_control_contract.sql`

## Backup Executado

Backup real executado em staging:

- Arquivo: `/tmp/extreme-phase1/backups/extreme_staging-20260606-152643.sql.gz`
- Checksum: `/tmp/extreme-phase1/backups/extreme_staging-20260606-152643.sql.gz.sha256`
- Log: `/tmp/extreme-phase1/logs/backup-20260606-152643.log`
- SHA-256: `250ea22ce664fd7e91430122351a6a37efd60caf9385924993ed8434d9ec4b5b`
- Verificacao: gzip OK, checksum OK, dump legivel.

## Restore Testado

Restore real executado em banco separado, sem sobrescrever o staging original:

- Banco alvo: `extreme_restore`
- Resultado: restore concluido sem erro.
- Contagens restauradas: `alunos=0`, `matriculas=0`, `pagamentos=0`, `check_ins=0`.
- Flyway restaurado com versoes `1`, `2` e `3` em sucesso.

Observacao: as contagens zeradas sao esperadas porque o staging validado estava vazio.

## Smoke Test Executado

Smoke basico combinado passou:

```bash
API_BASE_URL=http://127.0.0.1:18080 \
GATEWAY_BASE_URL=http://127.0.0.1:14000 \
CHECK_GATEWAY=true \
scripts/smoke-test.sh
```

Resultado:

- API root respondeu.
- Gateway `/health` respondeu.
- Gateway `/status` respondeu.
- Smoke basico passou.

Smoke autenticado nao passou:

- `POST /auth/login` retornou `401`.
- Causa confirmada: tabela `usuarios` vazia em staging.
- O inicializador `AdminUserInitializer` roda apenas nos profiles `dev | local`, portanto nao cria admin em `prod`.

## Gateway Validado

Correcoes tecnicas aplicadas para permitir runtime real do gateway:

- imports ESM em runtime apontam para arquivos `.js`/`index.js`;
- Fastify 5 usa `loggerInstance` para receber a instancia Pino;
- hook de API key admin foi limitado a paths `/admin/*`, liberando `/health` e `/status`.

Validacao:

- `GET /health`: `200 OK`.
- `GET /status`: `200 OK`, com `offlineMode=true`, `offlineStrict=true`, `pendingEvents=0`.
- `POST /admin/heartbeat` com API key errada: `401`.
- `POST /admin/heartbeat` com API key correta: `200`, corpo `{"ok":false}`.

Pendencia:

- `ok=false` ocorre porque o backend responde `401` para heartbeat/snapshot com a credencial de dispositivo usada no teste. Isso exige provisionamento real de dispositivo/API key antes do piloto.

## Alertas e Observabilidade

Documentacao existente revisada:

- `docs/runbooks/observability.md`
- `docs/runbooks/deploy.md`
- `docs/runbooks/rollback.md`

Status: condicionado.

Ainda falta configurar, fora do codigo, o canal real de alerta, o responsavel de plantao e a regra de acionamento para P0/P1. Sem isso, o piloto nao deve abrir para operacao real.

## Plano Manual

Plano existente revisado:

- `docs/runbooks/manual-continuity-plan.md`

Status: condicionado.

O plano existe, mas ainda precisa de validacao operacional: responsavel nomeado, recepcao orientada e ensaio manual registrado.

## Artefatos Criados

- `docs/runbooks/daily-pilot-checklist.md`
- `docs/runbooks/pilot-incident-report.md`
- `docs/phase-1-pilot-go-no-go-report.md`

## Riscos Restantes

- Sem conta ADMIN/operacional em profile `prod`, nao ha login de recepcao/admin no staging.
- Sem dispositivo/API key provisionado, gateway nao sincroniza snapshot nem heartbeat com sucesso.
- Alertas e responsavel operacional ainda nao foram comprovados.
- Plano manual ainda precisa de treinamento/aceite da recepcao.
- Ambiente local exigiu execucao fora do sandbox para Docker, portas locais e gateway.

## Condicoes Para Retomar Go/No-Go

- Provisionar conta ADMIN/operacional por procedimento seguro para staging/prod.
- Provisionar dispositivo de acesso e chave valida para o gateway.
- Reexecutar smoke autenticado e smoke de gateway com heartbeat/snapshot aceitos.
- Registrar responsavel, canal de alerta e escala de acionamento.
- Fazer ensaio do plano manual com a recepcao.
- Rodar backup e restore novamente apos provisionamento.
