# Operacao do gateway de acesso

Este documento registra o estado operacional do gateway para a Fase 1.1.

## Estado atual

O workspace contem apenas artefatos compilados/parciais do gateway (`gateway/dist` e `gateway/node_modules`). Nao ha `gateway/src` nem `gateway/package.json` no checkout atual, entao nao e possivel validar type-check, lint, build ou testes do gateway a partir da fonte.

Pelos artefatos compilados, o gateway espera que o backend exponha:

- `GET /controle-acesso/snapshot-autorizados`
- `POST /controle-acesso/validar-dispositivo`
- `POST /dispositivos-acesso/{deviceId}/heartbeat`
- `POST /eventos-acesso/sincronizar-lote`

As chamadas usam:

- `X-Device-Api-Key`
- `X-Device-Id` ou `X-Gateway-Id`
- `X-Request-Timestamp`

Essas rotas existem no backend. `X-Gateway-Id` e aceito como alias de compatibilidade para o ID do dispositivo.

## Requisitos para operacao real

- Dispositivo cadastrado no backend com status ativo.
- API key gerada uma unica vez e armazenada somente como hash BCrypt no backend.
- `DEVICE_ID` e `DEVICE_API_KEY` configurados no ambiente local do gateway.
- `ADMIN_API_KEY` do gateway configurada fora do Git para acoes administrativas internas.
- `/health` e `/status` liberados sem vazamento de segredo.
- Snapshot e heartbeat validados sem 401.

## Bloqueio restante

Gateway nao pode ser considerado pronto para piloto enquanto o codigo-fonte do gateway nao estiver rastreavel para testes e enquanto snapshot/heartbeat nao forem validados no ambiente de staging real.
