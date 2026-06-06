# Phase 1.1 - Go/No-Go do piloto controlado

Data: 2026-06-06

## Diagnostico

O checkout atual passou a conter o contrato minimo de backend para gateway local: `DispositivoAcesso`, API key tecnica, heartbeat, snapshot autorizado, validacao por dispositivo, credenciais de acesso e eventos de acesso com idempotencia.

Tambem nao existiam os runbooks/checklists citados pelo prompt nem `docker-compose.staging.yml`; eles foram criados/adaptados para o estado real do projeto.

## Status anterior

NO-GO para piloto operacional completo por ausencia de usuario ADMIN em staging e ausencia de dispositivo/API key provisionado para gateway.

## Provisionamento ADMIN

Implementado bootstrap controlado por variavel:

- `APP_BOOTSTRAP_ADMIN_ENABLED=true`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_USERNAME`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`

O bootstrap:

- roda em qualquer profile somente quando a flag explicita esta ativa;
- exige email, username e senha;
- cria somente se nao existir usuario com role `ADMIN`;
- salva senha com BCrypt;
- nao imprime senha em log;
- deve ser desligado apos o primeiro uso.

Status: pronto para executar em staging.

## Login autenticado

Status local: coberto por testes automatizados e por `scripts/smoke-test-authenticated.sh`.

Status staging real: pendente de execucao com credenciais reais fora do Git.

## Dispositivo/gateway

Status: implementado no backend, pendente de execucao em staging real.

Contrato disponivel:

- `DispositivoAcesso` com API key armazenada somente como BCrypt hash;
- autenticacao tecnica por `X-Device-Api-Key`;
- `GET /controle-acesso/snapshot-autorizados`;
- `POST /controle-acesso/validar-dispositivo`;
- `POST /dispositivos-acesso/{id}/heartbeat`;
- `POST /eventos-acesso/sincronizar-lote`.

Nao foi criado mock nem atalho inseguro.

## Smoke autenticado

Criado `scripts/smoke-test-authenticated.sh` para validar:

- API root;
- login ADMIN;
- JWT recebido com mascara;
- listagem autenticada de alunos;
- listagem autenticada de planos;
- listagem autenticada de matriculas;
- listagem autenticada de pagamentos;
- listagem autenticada de check-ins;
- provisionamento tecnico de dispositivo;
- heartbeat tecnico;
- snapshot autorizado;
- sincronizacao de eventos;
- bloqueio de API key ausente/invalida;
- Swagger protegido/desabilitado;
- gateway health/status quando `GATEWAY_BASE_URL` estiver definido;
- snapshot/heartbeat via gateway quando `GATEWAY_ADMIN_API_KEY` estiver definido.

O script nao imprime senha, API key ou JWT completo.

## RBAC

Validacao automatizada disponivel para ADMIN nas rotas existentes via smoke autenticado.

Dispositivos exigem ADMIN. Credenciais e consulta de eventos aceitam ADMIN/RECEPCAO. Rotas tecnicas exigem API key de dispositivo. Busca global permanece fora do escopo desta entrega.

## Segredos

Segredos reais nao foram adicionados. `.env.example`, compose e docs usam placeholders ou nomes de variaveis.

## Decisao

NO-GO operacional ate staging real.

Justificativa: o contrato backend foi implementado sem mock e sem biometria, mas o criterio de aceite de piloto exige execucao em staging real com credenciais fora do Git e gateway operacional.

## Riscos restantes

- Provisionamento real de dispositivo/API key em staging pendente.
- Snapshot e heartbeat em staging real pendentes.
- Busca global nao implementada nesta entrega.
- Gateway presente no workspace esta incompleto como fonte rastreavel: existe `gateway/dist` e `node_modules`, mas nao `gateway/src` nem `gateway/package.json`.

## Proxima fase recomendada

Executar smoke autenticado em staging real e registrar evidencias antes de reavaliar GO condicionado.
