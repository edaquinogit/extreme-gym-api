# Runbook: Backup PostgreSQL

## Objetivo

Garantir que dados de alunos, matriculas, pagamentos, check-ins e eventos de acesso possam ser recuperados.

RPO recomendado: ate 24h.

## Variaveis

Configure no ambiente do processo, nunca em arquivo versionado:

```bash
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=extreme_db
POSTGRES_USER=extreme_user
POSTGRES_PASSWORD=...
BACKUP_DIR=./backups/postgres
BACKUP_RETENTION_DAYS=14
BACKUP_LOG_DIR=./logs/backups
```

`BACKUP_RETENTION_DAYS` deve ficar entre 7 e 30 dias.

Se `pg_dump` nao estiver instalado no host, use o cliente PostgreSQL dentro do container:

```bash
POSTGRES_DOCKER_CONTAINER=extreme-gym-api-postgres-1
POSTGRES_HOST=127.0.0.1
```

## Execucao Manual

```bash
scripts/backup-postgres.sh
```

O script gera:

- backup com timestamp em `BACKUP_DIR`;
- arquivo `.sha256`, quando `sha256sum` estiver disponivel;
- log em `BACKUP_LOG_DIR`;
- remocao automatica de backups antigos conforme retencao.

## Execucao com Docker Compose

Se o PostgreSQL estiver acessivel apenas na rede interna do Compose, rode o script de uma maquina/container que consiga resolver o host `postgres`, ou mapeie temporariamente o acesso em ambiente controlado. Nao exponha o banco publicamente para facilitar backup.

## Cron Sugerido

```cron
0 2 * * * cd /caminho/extreme-gym-api && /usr/bin/env bash scripts/backup-postgres.sh
```

## Validacao

Depois do backup:

```bash
scripts/verify-backup.sh backups/postgres/arquivo.sql.gz
```

O backup so deve ser considerado valido quando:

- arquivo existe;
- arquivo nao esta vazio;
- `gzip -t` passa;
- checksum passa, se existir;
- log registra sucesso.

## Alerta

Falha de backup e incidente **P0**. A falha deve gerar alerta para o responsavel tecnico no mesmo dia.
