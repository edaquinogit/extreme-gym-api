# Daily Pilot Checklist

Checklist operacional para o piloto controlado da Extreme Gym API em staging operacional.

Data/hora local de referencia inicial: 2026-06-06 15:54:11 -03.

## Antes da abertura

- [ ] Confirmar responsavel do dia pela operacao do piloto.
- [ ] Confirmar canal de acionamento rapido entre recepcao, suporte tecnico e decisor.
- [ ] Confirmar que a recepcao conhece o plano manual em `docs/runbooks/manual-continuity-plan.md`.
- [ ] Confirmar staging ativo e saudavel:

```bash
docker compose -f docker-compose.staging.yml ps
```

- [ ] Confirmar API:

```bash
curl --fail http://127.0.0.1:18080/
```

- [ ] Confirmar gateway local, quando estiver em uso:

```bash
curl --fail http://127.0.0.1:14000/health
curl --fail http://127.0.0.1:14000/status
```

- [ ] Confirmar login ADMIN ou conta operacional provisionada no ambiente.
- [ ] Confirmar credencial de dispositivo/gateway provisionada no backend.
- [ ] Executar smoke basico:

```bash
API_BASE_URL=http://127.0.0.1:18080 \
GATEWAY_BASE_URL=http://127.0.0.1:14000 \
CHECK_GATEWAY=true \
scripts/smoke-test.sh
```

- [ ] Confirmar backup do dia ou backup anterior valido:

```bash
scripts/verify-backup.sh /path/do/backup.sql.gz
```

## Durante o piloto

- [ ] Monitorar logs da API.
- [ ] Monitorar logs do gateway.
- [ ] Registrar falhas de login, 401 inesperado, 5xx, lentidao e divergencias de recepcao.
- [ ] Se houver falha de sistema que afete atendimento, ativar plano manual.
- [ ] Nao testar Face ID, hardware real ou regras fora do escopo do piloto.
- [ ] Nao promover para producao durante o piloto.

## Fechamento do dia

- [ ] Registrar incidentes e quase-incidentes.
- [ ] Rodar backup e verificar checksum.
- [ ] Registrar se houve uso do plano manual.
- [ ] Registrar tempo total de indisponibilidade ou degradacao.
- [ ] Atualizar decisao de continuidade do piloto: continuar, continuar condicionado, pausar.

## Criterios de pausa imediata

- P0: perda de dados, corrupcao de banco, restauracao impossivel ou vazamento de segredo.
- P1: API indisponivel para fluxo essencial, login operacional impossivel, gateway sem iniciar, ou recepcao sem plano manual.
- P1: backup do dia falha e nao ha backup valido recente.
- P1: alertas/responsavel indisponiveis durante janela operacional.
