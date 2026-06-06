# Deploy em staging

## Pre-requisitos

- Segredos definidos fora do Git.
- `AUTH_REGISTRATION_ENABLED=false`.
- `APP_BOOTSTRAP_ADMIN_ENABLED=true` apenas no primeiro provisionamento.
- `JWT_SECRET` forte gerado para o ambiente.
- `CORS_ALLOWED_ORIGINS` restrito ao frontend esperado.

## Validar compose

```bash
docker compose -f docker-compose.staging.yml config
```

## Subir staging

```bash
docker compose -f docker-compose.staging.yml up -d --build
```

## Provisionar ADMIN inicial

Siga `docs/runbooks/provisioning-staging.md`.

## Validar

```bash
API_BASE_URL=<url-staging> \
ADMIN_LOGIN=<login-operacional> \
ADMIN_PASSWORD=<senha-forte> \
scripts/smoke-test-authenticated.sh
```

## Pos-deploy

- Desligar `APP_BOOTSTRAP_ADMIN_ENABLED`.
- Conferir logs sem segredos.
- Registrar resultado em `docs/phase-1-pilot-go-no-go-report.md`.
