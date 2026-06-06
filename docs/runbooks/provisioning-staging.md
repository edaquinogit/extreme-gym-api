# Provisionamento controlado em staging

Este runbook descreve o provisionamento inicial seguro para staging. Ele nao deve ser usado para abrir registro publico, commitar senha, commitar API key ou criar dados falsos em producao.

## ADMIN inicial

1. Defina os segredos no ambiente de staging, fora do Git:

```bash
APP_BOOTSTRAP_ADMIN_ENABLED=true
APP_BOOTSTRAP_ADMIN_EMAIL=<email-operacional>
APP_BOOTSTRAP_ADMIN_USERNAME=<login-operacional>
APP_BOOTSTRAP_ADMIN_PASSWORD=<senha-forte>
AUTH_REGISTRATION_ENABLED=false
```

2. Suba ou reinicie a aplicacao com profile `prod`.
3. Confirme no log apenas a mensagem de criacao do usuario ADMIN. A senha nao deve aparecer.
4. Valide o login:

```bash
curl -sS -X POST "$API_BASE_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"login":"<login-operacional>","password":"<senha-forte>"}'
```

5. Desabilite o bootstrap imediatamente apos a criacao:

```bash
APP_BOOTSTRAP_ADMIN_ENABLED=false
```

O initializer nao cria duplicado se ja existir qualquer usuario com role `ADMIN`.

## Usuario operacional

Neste checkout, o registro publico permanece desabilitado por padrao. Caso seja necessario criar usuario RECEPCAO/CATRACA/PROFESSOR em staging, use fluxo administrativo autenticado quando ele existir. Nao habilite `AUTH_REGISTRATION_ENABLED=true` em staging/prod para contornar provisionamento.

## Dispositivo gateway

1. Criar dispositivo por endpoint administrativo autenticado com JWT ADMIN.
2. Gerar uma API key uma unica vez.
3. Armazenar somente hash BCrypt no backend.
4. Copiar a API key para `gateway/.env` local de staging, nunca para Git.
5. Configurar `DEVICE_ID`, `DEVICE_API_KEY`, `BACKEND_BASE_URL` e `ADMIN_API_KEY` no ambiente do gateway.
6. Validar snapshot e heartbeat sem 401.

O backend aceita `X-Device-Id` e `X-Gateway-Id` como identificador tecnico do dispositivo. A API key deve seguir em `X-Device-Api-Key`.

## Rotacao

Para rotacionar senha ADMIN:

1. Criar nova senha forte no cofre operacional.
2. Atualizar pelo fluxo administrativo ou por procedimento controlado de banco com hash BCrypt.
3. Invalidar a senha anterior.
4. Rodar `scripts/smoke-test-authenticated.sh`.

Para rotacionar API key do dispositivo:

1. Gerar nova API key.
2. Atualizar somente o hash no backend.
3. Atualizar `gateway/.env` no host de staging.
4. Reiniciar o gateway.
5. Validar snapshot e heartbeat.

Para revogar dispositivo:

1. Marcar dispositivo como `INATIVO` no backend.
2. Remover a API key do ambiente do gateway.
3. Confirmar que chamadas com a chave antiga retornam 401/403.

## Validacoes

```bash
API_BASE_URL=https://staging.example.com \
ADMIN_LOGIN=<login-operacional> \
ADMIN_PASSWORD=<senha-forte> \
scripts/smoke-test-authenticated.sh
```

Com gateway configurado:

```bash
API_BASE_URL=https://staging.example.com \
ADMIN_LOGIN=<login-operacional> \
ADMIN_PASSWORD=<senha-forte> \
GATEWAY_BASE_URL=http://gateway-staging:4000 \
GATEWAY_ADMIN_API_KEY=<admin-api-key-do-gateway> \
scripts/smoke-test-authenticated.sh
```

## Nunca commitar

- `.env`
- `gateway/.env`
- senha ADMIN
- JWT real
- `DEVICE_API_KEY`
- `ADMIN_API_KEY`
- `HMAC_SECRET`
- hash reutilizavel como credencial operacional permanente

## Checklist pos-provisionamento

- ADMIN criado com bootstrap explicito.
- Login ADMIN retorna JWT.
- `APP_BOOTSTRAP_ADMIN_ENABLED=false` apos o primeiro uso.
- Registro publico segue desabilitado.
- Swagger segue protegido/desabilitado em profile `prod`.
- Nenhum segredo aparece em logs ou documentos.
- Gateway provisionado com API key gerada uma unica vez.
- Smoke autenticado executado e registrado.
