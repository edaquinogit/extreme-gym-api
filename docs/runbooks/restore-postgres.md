# Runbook: Restore PostgreSQL

## Objetivo

Restaurar um backup em ambiente separado para validar continuidade. Nao restaure em producao sem decisao explicita.

RTO recomendado: ate 4h.

## Escolher Backup

1. Liste arquivos em `BACKUP_DIR`.
2. Escolha o backup mais recente anterior ao incidente.
3. Verifique integridade:

```bash
scripts/verify-backup.sh backups/postgres/extreme_db-YYYYMMDD-HHMMSS.sql.gz
```

## Restore Seguro em Staging

Use banco descartavel ou staging. Exemplo:

```bash
RESTORE_TARGET_ENV=staging \
RESTORE_CONFIRM=RESTORE_STAGING \
POSTGRES_HOST=localhost \
POSTGRES_PORT=5432 \
POSTGRES_DB=extreme_restore \
POSTGRES_USER=extreme_user \
POSTGRES_PASSWORD=... \
scripts/restore-postgres.sh backups/postgres/extreme_db-YYYYMMDD-HHMMSS.sql.gz
```

Se `psql` nao estiver instalado no host, informe `POSTGRES_DOCKER_CONTAINER` e use o cliente dentro do container PostgreSQL de staging.

O script recusa restore sem confirmacao explicita.

## Validacoes Pos-Restore

Execute no banco restaurado:

```sql
select count(*) from aluno;
select count(*) from matricula;
select count(*) from pagamento;
select count(*) from check_in;
select installed_rank, version, script, success from flyway_schema_history order by installed_rank;
```

Depois suba a aplicacao apontando para o banco restaurado e valide:

- login ADMIN;
- listagem de alunos;
- matriculas ativas;
- pagamentos;
- validacao de acesso liberado;
- validacao de acesso bloqueado;
- dashboard;
- eventos de acesso, quando existirem.

## Proibicoes

- Nao apontar `POSTGRES_DB` para producao sem aprovacao formal.
- Nao sobrescrever producao para "testar".
- Nao restaurar backup antigo sem comunicar impacto.
- Nao seguir com go-live se restore nunca foi ensaiado.
