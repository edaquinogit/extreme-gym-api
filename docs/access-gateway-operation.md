# Operacao do Gateway de Acesso

## Escopo Atual

O gateway local e vendor-agnostic e ainda nao integra hardware real em producao. Nesta Phase 0, ele deve ser operado em ambiente controlado para validar snapshot, eventos pendentes, heartbeat e sync.

## Iniciar

```bash
cd gateway
npm ci
npm run build
npm start
```

Variaveis principais:

- `BACKEND_BASE_URL`
- `GATEWAY_ID`
- `GATEWAY_NAME`
- `DEVICE_ID`
- `DEVICE_API_KEY`
- `DEVICE_HMAC_SECRET`
- `ADMIN_API_KEY`
- `GATEWAY_PORT`
- `GATEWAY_DATA_PATH`

## Parar e Reiniciar

Em terminal:

```bash
Ctrl+C
```

Depois:

```bash
npm start
```

Em producao assistida, preferir gerenciador de processo ou container com restart policy.

## Health e Status

```bash
curl http://localhost:4000/health
curl http://localhost:4000/status
```

`/status` mostra:

- id/nome do gateway;
- backend configurado;
- modo offline;
- eventos pendentes.

## Acoes Administrativas

Todas exigem header `x-admin-api-key`.

```bash
curl -X POST http://localhost:4000/admin/heartbeat \
  -H "x-admin-api-key: $ADMIN_API_KEY"

curl -X POST http://localhost:4000/admin/snapshot-refresh \
  -H "x-admin-api-key: $ADMIN_API_KEY"

curl -X POST http://localhost:4000/admin/sync \
  -H "x-admin-api-key: $ADMIN_API_KEY"
```

## Backend Offline

O gateway tenta validar online e cai para snapshot local quando offline mode esta habilitado. Durante piloto:

- recepcao deve manter plano manual;
- eventos pendentes devem ser conferidos em `/status`;
- sync deve ser forcado quando backend voltar;
- snapshot expirado deve bloquear de forma conservadora.

## SQLite Local

`GATEWAY_DATA_PATH` define o arquivo SQLite. Mantenha esse arquivo persistido fora de containers descartaveis.

Nao copie banco local para terceiros. Ele pode conter historico operacional.

## Rotacao de Chaves

- `ADMIN_API_KEY`: usada para operacao administrativa do gateway.
- `DEVICE_API_KEY`: usada na comunicacao com backend.
- `DEVICE_HMAC_SECRET`: usada para assinatura/validacao quando aplicavel.

Rotacione em janela controlada:

1. atualizar backend/configuracao segura;
2. atualizar gateway;
3. reiniciar gateway;
4. validar heartbeat, snapshot e sync.
