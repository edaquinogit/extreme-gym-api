# Sprint backend - contrato de controle de acesso

## Diagnostico inicial

O backend usa arquitetura simples em camadas: `entity`, `repository`, `service`, `controller`, `dto`, `enums`, `security` e `exception`. A regra atual de acesso esta centralizada em `AcessoService.validarAluno(Long)`, que avalia status do aluno, matricula ativa, validade da matricula e pagamento pago.

As migrations Flyway existentes estavam em `src/main/resources/db/migration`, com `V1` e `V2`. O perfil de teste usa H2 com `ddl-auto=create-drop` e seguranca desabilitada.

## Decisao arquitetural

A sprint adicionou um modulo backend real para controle de acesso fisico, mas sem dependencia de fabricante. A validacao por dispositivo orquestra dispositivo, credencial e evento, e reaproveita `AcessoService.validarAluno(Long)` para nao duplicar regra de negocio.

API key por dispositivo foi implementada como seguranca tecnica adicional, com hash BCrypt e header `X-Device-Api-Key`.

## Entidades

- `DispositivoAcesso`
- `EventoAcesso`
- `CredencialAcesso`
- `AuditoriaAcesso`

## Enums

- `TipoDispositivoAcesso`
- `StatusDispositivoAcesso`
- `ModoOperacaoDispositivo`
- `OrigemEventoAcesso`
- `ModoEventoAcesso`
- `ResultadoAcesso`
- `TipoCredencialAcesso`
- `StatusCredencialAcesso`

## Migrations

- `V3__create_access_control_contract.sql`

Tabelas criadas:

- `dispositivos_acesso`
- `eventos_acesso`
- `credenciais_acesso`
- `auditoria_acesso`

## Endpoints

- `GET /dispositivos-acesso`
- `GET /dispositivos-acesso/{id}`
- `POST /dispositivos-acesso`
- `PUT /dispositivos-acesso/{id}`
- `PATCH /dispositivos-acesso/{id}/status`
- `POST /dispositivos-acesso/{id}/heartbeat`
- `GET /eventos-acesso`
- `GET /eventos-acesso/hoje`
- `GET /eventos-acesso/aluno/{alunoId}`
- `GET /eventos-acesso/dispositivo/{dispositivoId}`
- `POST /eventos-acesso`
- `POST /eventos-acesso/sincronizar-lote`
- `GET /alunos/{alunoId}/credenciais-acesso`
- `POST /alunos/{alunoId}/credenciais-acesso`
- `PATCH /credenciais-acesso/{id}/revogar`
- `POST /controle-acesso/validar-dispositivo`
- `GET /controle-acesso/snapshot-autorizados`
- `GET /controle-acesso/snapshot-autorizados/{dispositivoId}`
- `GET /busca-global`

## Seguranca

- RBAC atualizado no `SecurityConfig`.
- API key por dispositivo via `X-Device-Api-Key`.
- Hash BCrypt para segredo tecnico.
- Default seguro em runtime: `ACCESS_DEVICE_API_KEY_REQUIRED=true`.
- Perfil `test` desabilita API key para testes unitarios/integracao, seguindo o padrao atual de seguranca desabilitada.

## Auditoria

Implementada auditoria minima em `AuditoriaAcessoService` e tabela `auditoria_acesso`.

## Snapshot

Implementado snapshot minimo de autorizados. Ele nao retorna dados sensiveis e usa a regra atual de acesso para calcular `liberado` e `motivoBloqueio`.

## Busca global

Implementada busca global server-side com termo minimo, limite por grupo e exclusao de pagamentos para perfil `CATRACA`.

## Testes

Criado `AccessControlContractServiceTest`, cobrindo:

- criacao de dispositivo valido;
- bloqueio de dispositivo duplicado;
- heartbeat;
- dispositivo inativo sem validacao;
- evento liberado;
- evento bloqueado/idempotente;
- validacao por dispositivo reutilizando regra atual;
- snapshot minimo;
- credencial revogada fora do snapshot;
- busca global com termo minimo;
- busca global sem financeiro para `CATRACA`.

## Contratos preservados

- `POST /acessos/validar` foi preservado.
- Services atuais de aluno, plano, matricula, pagamento, check-in e acesso foram mantidos.
- Regras atuais de acesso foram reaproveitadas.

## Nao implementado propositalmente

- Integracao com catraca fisica.
- Gateway local.
- Reconhecimento facial proprio.
- Armazenamento de imagem facial.
- Armazenamento de template biometrico real.
- Payload especifico de fabricante.
- Cache offline local.
- Token ADMIN fixo.

## Proximo prompt recomendado

Implementar homologacao do gateway local apenas depois de definir fabricante/modelo/API oficial da catraca, incluindo HMAC, janela de timestamp, contrato final de lote offline e testes com equipamento real.
