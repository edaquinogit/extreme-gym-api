# Runbook: Observabilidade Minima

## Objetivo

Detectar indisponibilidade antes que a recepcao descubra no atendimento.

## Healthchecks

Backend:

```text
GET /
```

Resposta esperada:

```json
{"message":"Extreme Gym API is running"}
```

Gateway:

```text
GET /health
GET /status
```

Frontend:

- abrir a URL publica;
- validar carregamento do bundle;
- validar tela de login.

## Logs

Backend:

```bash
docker compose logs app --tail=200
```

Gateway:

```bash
cd gateway
npm start
```

Em producao, rode via gerenciador de processo ou container com stdout coletado e retencao configurada.

Backup:

```text
logs/backups/backup-*.log
```

## Alertas Minimos

Configure ferramenta simples como Uptime Kuma, Better Stack, Grafana Cloud, Sentry ou monitoramento do provedor para:

- API fora do ar por mais de 2 minutos;
- frontend fora do ar;
- gateway sem resposta;
- disco acima de 80%;
- backup sem sucesso nas ultimas 25h;
- muitos erros 5xx;
- eventos pendentes do gateway crescendo.

## Severidade

P0:

- banco indisponivel;
- backup falhou;
- restore necessario;
- API fora do ar em horario de atendimento;
- sistema bloqueando entrada sem contingencia.

P1:

- gateway offline com liberacao manual funcionando;
- lentidao recorrente;
- sync atrasado;
- erro em fluxo financeiro.

P2:

- falha visual sem impacto operacional;
- melhoria de dashboard;
- ajuste de texto.

## Acionamento

O alerta deve conter:

- ambiente;
- horario;
- endpoint afetado;
- ultimo deploy;
- link de logs;
- responsavel tecnico.
