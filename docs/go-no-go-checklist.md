# Checklist Go/No-Go

## Criterios para GO condicionado

- ADMIN inicial provisionado com bootstrap explicito.
- Bootstrap desligado apos o primeiro uso.
- Login ADMIN retorna JWT.
- Rotas protegidas existentes respondem com JWT.
- Registro publico permanece desabilitado.
- Swagger permanece protegido/desabilitado em profile `prod`.
- Nenhum segredo real em Git, docs, compose ou logs.
- Backup/restore real documentado e aprovado.
- Plano de continuidade manual documentado.
- Dispositivo gateway provisionado com API key segura.
- Snapshot do gateway sem 401.
- Heartbeat do gateway sem 401.
- Smoke autenticado aprovado.
- RBAC critico aprovado.

## Criterios de NO-GO

- Login ADMIN segue 401 por ausencia de usuario.
- Bootstrap depende de senha hardcoded ou registro publico.
- Snapshot/heartbeat seguem 401/404.
- Segredo real aparece em Git, docs ou logs.
- RBAC critico falha.
- Gateway depende de mock em staging real.

## Status deste checkout

- ADMIN bootstrap seguro: pronto.
- Smoke autenticado backend: criado.
- Backend de dispositivo/gateway: implementado.
- Snapshot/heartbeat locais: validaveis pelo smoke.
- Snapshot/heartbeat em staging real: pendentes.
- Decisao atual: NO-GO operacional ate execucao em staging real.
