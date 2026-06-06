# Runbook: Deploy

## Janela

Evite horario de pico. Deploy de producao deve ocorrer com responsavel tecnico disponivel.

## Pre-check

- Branch/tag revisada.
- `git status -sb` sem mudancas inesperadas.
- Secrets configurados fora do repositorio.
- `AUTH_REGISTRATION_ENABLED=false`.
- `CORS_ALLOWED_ORIGINS` com dominio real.
- PostgreSQL sem porta publica.
- Backup recente disponivel.

## Validacoes Antes do Deploy

```bash
./mvnw test
cd frontend && npm run type-check && npm run lint && npm run test && npm run build
cd ../gateway && npm run type-check && npm run lint && npm run test && npm run build && npm audit
```

## Backup Antes de Migration

```bash
scripts/backup-postgres.sh
```

Nao rode migration destrutiva sem backup.

## Deploy com Compose

```bash
docker compose -f docker-compose.prod.example.yml config
docker compose -f docker-compose.prod.example.yml build
docker compose -f docker-compose.prod.example.yml up -d
docker compose -f docker-compose.prod.example.yml ps
```

Flyway executa migrations pendentes no startup da aplicacao Spring Boot.

## Smoke Test Pos-Deploy

Use [smoke-test.md](smoke-test.md).

Minimo:

- API responde `/`;
- frontend abre;
- login ADMIN funciona;
- dashboard carrega;
- aluno, matricula, pagamento e check-in funcionam;
- gateway `/health` e `/status` respondem;
- logs sem erro recorrente.

## Registro

Registre:

- data/hora;
- commit/tag;
- responsavel;
- backup usado;
- resultado do smoke test;
- incidentes.
