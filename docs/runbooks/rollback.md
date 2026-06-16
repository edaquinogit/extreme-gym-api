# Runbook: Rollback

## Quando Abortar

Abortar ou reverter deploy se:

- API nao sobe;
- login quebra;
- erro 5xx recorrente;
- migrations falham;
- pagamentos/check-ins falham;
- acesso bloqueia alunos ativos sem caminho manual;
- gateway para de sincronizar e recepcao nao consegue contingenciar.

## Voltar Imagem Anterior

1. Comunique a equipe.
2. Pare novos deploys.
3. Identifique a imagem/tag anterior.
4. Suba a versao anterior.
5. Rode smoke test.

```bash
docker compose -f docker-compose.prod.example.yml up -d app
```

## Migrations

Rollback de codigo nem sempre desfaz migration.

Regras:

- Migration destrutiva exige backup imediatamente antes.
- Se a migration alterou dados/schema de forma incompativel, restaure backup em ambiente separado antes de tocar producao.
- Nunca rode SQL manual em producao sem plano escrito.

## Restore

Se for necessario restaurar dados, siga [restore-postgres.md](restore-postgres.md). Restore em producao exige confirmacao explicita e comunicacao ao gestor.

## Validacao

Depois do rollback:

- API `/`;
- login;
- dashboard;
- alunos;
- matriculas;
- pagamentos;
- check-ins;
- validacao de acesso;
- gateway `/status`;
- logs.
