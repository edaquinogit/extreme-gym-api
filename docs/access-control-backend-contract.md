# Contrato backend de controle de acesso

Este contrato entrega o minimo real para gateway local sem hardware, Face ID, webcam, imagem biometrica ou template biometrico.

## Dispositivo

Provisionamento administrativo:

```http
POST /dispositivos-acesso
Authorization: Bearer <jwt-admin>
```

Payload:

```json
{
  "nome": "Gateway recepcao",
  "tipo": "GATEWAY",
  "modoOperacao": "HIBRIDO",
  "identificadorExterno": "gateway-recepcao-01"
}
```

A resposta retorna `apiKeyPlaintext` uma unica vez. O backend armazena somente hash BCrypt e nunca lista a chave ou o hash.

Rotacao:

```http
POST /dispositivos-acesso/{id}/rotate-api-key
Authorization: Bearer <jwt-admin>
```

## Autenticacao tecnica

As rotas tecnicas nao aceitam JWT humano como substituto de credencial tecnica. Use:

- `X-Device-Api-Key`
- `X-Device-Id` ou `X-Gateway-Id`

`X-Gateway-Id` e aceito como alias operacional para compatibilidade com artefatos antigos do gateway.

## Heartbeat

```http
POST /dispositivos-acesso/{id}/heartbeat
X-Device-Api-Key: <device-api-key>
```

Atualiza `ultimaComunicacaoEm` e pode informar `status` e `modoOperacao`.

## Snapshot autorizado

```http
GET /controle-acesso/snapshot-autorizados
X-Device-Id: <device-id>
X-Device-Api-Key: <device-api-key>
```

O snapshot usa `AcessoService.validarAluno(Long)` para reaproveitar a regra real de matricula, status do aluno e pagamento. A resposta nao inclui nome, documento, telefone, email ou entidade completa do aluno.

## Validacao online

```http
POST /controle-acesso/validar-dispositivo
X-Device-Id: <device-id>
X-Device-Api-Key: <device-api-key>
```

Payload:

```json
{
  "credencialTipo": "QRCODE",
  "identificadorExterno": "qr-123",
  "origem": "DISPOSITIVO",
  "idempotencyKey": "gateway-01-2026-06-06T12:00:00Z-1",
  "dataHoraEvento": "2026-06-06T12:00:00"
}
```

A validacao grava `EventoAcesso` com idempotencia por `idempotencyKey`.

## Eventos offline

```http
POST /eventos-acesso/sincronizar-lote
X-Device-Id: <device-id>
X-Device-Api-Key: <device-api-key>
```

Eventos duplicados pela mesma `idempotencyKey` nao sao recriados.

## Credenciais

Rotas humanas para ADMIN/RECEPCAO:

- `GET /alunos/{alunoId}/credenciais-acesso`
- `POST /alunos/{alunoId}/credenciais-acesso`
- `PATCH /credenciais-acesso/{id}/revogar`

Credenciais armazenam apenas referencia externa segura. Dados brutos biometricos, imagens base64 e templates biometricos nao fazem parte deste contrato.

## RBAC

- ADMIN gerencia dispositivos.
- ADMIN/RECEPCAO gerenciam credenciais e consultam eventos.
- Rotas tecnicas exigem API key do dispositivo.
- CATRACA continua sem acesso financeiro ou administrativo.

## Fora do escopo desta entrega

- Hardware fisico.
- Face ID, webcam, foto, template ou matching biometrico.
- Busca global. Deve ser implementada em fase propria para evitar acoplamento prematuro.
