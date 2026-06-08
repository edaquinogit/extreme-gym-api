# Integração frontend de controle de acesso

## Resultado

O frontend consome apenas endpoints humanos autenticados por JWT para a operação de controle de acesso. Não há uso de API key de dispositivo no browser.

## Endpoints consumidos

- `GET /dispositivos-acesso`
- `POST /dispositivos-acesso`
- `GET /eventos-acesso`
- `GET /alunos/{alunoId}/credenciais-acesso`
- `POST /alunos/{alunoId}/credenciais-acesso`
- `PATCH /credenciais-acesso/{id}/revogar`

## Endpoints proibidos no frontend

- Heartbeat de dispositivo.
- Snapshot de autorizados.
- Sincronização em lote.
- Qualquer chamada com `X-Device-Api-Key`.

Esses contratos são de gateway/dispositivo e devem permanecer fora da aplicação web.

## Serviços e mappers

- `accessDeviceService` lista e cria dispositivos em `/dispositivos-acesso`.
- `accessEventService` lista eventos em `/eventos-acesso`.
- `accessCredentialService` lista, cria e revoga credenciais por aluno.
- Os mappers normalizam valores opcionais e protegem identificadores externos de credenciais, mantendo apenas versões mascaradas na UI.
- `apiKeyPlaintext` é usado somente no retorno imediato de criação de dispositivo, para exibição única em modal. O frontend não persiste nem reutiliza essa chave.

## Telas

- `/dispositivos-acesso`: disponível para `ADMIN`; permite cadastro de dispositivo e exibição única da API key gerada.
- `/eventos-acesso`: disponível para `ADMIN` e `RECEPCAO`; permite consultar eventos e filtrar por resultado, modo e origem.
- `Alunos`: abre o gerenciamento de credenciais do aluno, com listagem mascarada, criação e revogação para perfis autorizados.
- `/catraca`: exibe dispositivo e eventos recentes retornados pelos endpoints humanos, mantendo a validação manual existente sem endpoints técnicos de gateway.

## RBAC

- `ADMIN`: acesso a dispositivos, eventos, alunos, financeiro, matrículas, check-ins, acesso e catraca.
- `RECEPCAO`: acesso operacional a alunos, planos, matrículas, pagamentos, eventos, check-ins, acesso e catraca.
- `CATRACA`: acesso restrito a início, check-ins, acesso e catraca; sem financeiro, dispositivos ou credenciais.

## Pendências conhecidas

- `/busca-global` não existe no backend atual. O frontend não implementa busca global funcional enquanto o contrato não existir.
- Cadastro biométrico facial não foi implementado. A UI de credenciais oferece QR Code, PIN e cartão.
