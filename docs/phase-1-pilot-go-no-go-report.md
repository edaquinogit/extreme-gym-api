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

## Fase 1.2 - Validacao runtime local staging

Data: 2026-06-07

Ambiente utilizado:

- Docker Compose `docker-compose.staging.yml`;
- profile Spring `prod`;
- PostgreSQL real `postgres:16`;
- base local efemera `extreme_staging_local`;
- credenciais e segredos fornecidos apenas por variaveis de ambiente do processo, sem `.env` real versionado.

### Resultado do compose/staging

Status: parcialmente aprovado.

Evidencias:

- `docker compose -f docker-compose.staging.yml config --quiet` passou com variaveis efemeras;
- primeira subida reutilizou imagem Docker antiga e falhou conectando em `localhost:5432`;
- imagem do app foi reconstruida com `docker compose -f docker-compose.staging.yml build --no-cache app`;
- apos rebuild, app subiu com 9 repositories JPA detectados;
- Postgres ficou `healthy`;
- app ficou `healthy`;
- API root respondeu `200`;
- Flyway validou e aplicou 3 migrations, incluindo `V3__create_access_control_contract.sql`;
- Swagger permaneceu desabilitado/protegido pelo profile `prod`;
- logs revisados nao exibiram senha, JWT completo ou API key.

Durante a execucao foi identificado que `docker-compose.staging.yml` nao propagava as variaveis opcionais de rate limit. O compose foi ajustado para repassar:

- `AUTH_MAX_FAILED_ATTEMPTS`;
- `AUTH_LOCK_DURATION_MINUTES`.

### Provisionamento ADMIN

Status: parcialmente aprovado.

Evidencias:

- bootstrap criou usuario ADMIN com username operacional e email operacional;
- log exibiu apenas username/email e recomendacao para desligar bootstrap;
- senha nao apareceu nos logs;
- `APP_BOOTSTRAP_ADMIN_ENABLED` foi desligado apos provisionamento durante a validacao;
- consulta segura confirmou usuario ADMIN ativo;
- consulta segura confirmou hash BCrypt com tamanho 60 e prefixo BCrypt;
- comparacao booleana local confirmou que a senha operacional usada no teste corresponde ao hash salvo.

### Login ADMIN

Status: reprovado.

Resultado:

- `POST /auth/login` retornou `401 Credenciais invalidas` para username;
- `POST /auth/login` retornou `401 Credenciais invalidas` para email;
- payload com alias `email` tambem retornou `401`;
- apos tentativas diagnosticas, houve `429` por rate limit em uma rota de execucao local, mas o bloqueio principal reproduzido fora do sandbox permaneceu `401`.

Observacao:

O achado e bloqueante porque o ADMIN existe, esta ativo e o hash confere com a senha local usada, mas a autenticacao HTTP real nao emite JWT. Sem JWT, nao e correto prosseguir para criacao de dispositivo, heartbeat, snapshot, validacao por dispositivo ou sync lote como aprovados.

### Smoke autenticado real

Status: reprovado.

Resultado:

- API root passou;
- login ADMIN falhou com `401`;
- script interrompeu corretamente no primeiro fluxo principal quebrado;
- JWT completo nao foi impresso;
- API key nao foi criada nem impressa.

### Dispositivo, heartbeat, snapshot, validacao por dispositivo e sync lote

Status: nao executados nesta rodada.

Justificativa:

O criterio de seguranca da fase exige nao mascarar falha como sucesso. Como login ADMIN falhou, nao houve JWT administrativo para criar dispositivo e capturar API key tecnica. Portanto:

- criacao de dispositivo: nao aprovada;
- heartbeat real: nao aprovado;
- snapshot real: nao aprovado;
- validacao por dispositivo: nao aprovada;
- sync lote real: nao aprovado;
- testes negativos de API key: nao aprovados nesta rodada.

### Banco apos tentativa

Status validado parcialmente.

Evidencias seguras:

- usuario ADMIN existe e esta ativo;
- schema esta na migration v3;
- tabelas do contrato de access control foram criadas via Flyway;
- hash de senha existe em formato BCrypt;
- nenhum segredo plaintext foi persistido em arquivo versionado.

### Decisao Fase 1.2

NO-GO.

Justificativa:

Apesar de o ambiente staging local subir com PostgreSQL real e Flyway aplicar as migrations, o login ADMIN falha por HTTP real. Como o login e pre-condicao para provisionar dispositivo e API key, a validacao runtime do contrato do gateway nao pode ser considerada aprovada.

### Riscos restantes

- Investigar divergencia entre hash BCrypt confirmado no banco e falha de `POST /auth/login`.
- Reexecutar smoke completo somente depois que login ADMIN emitir JWT.
- Confirmar se ha diferenca de namespace/rede entre execucao sandbox, execucao fora do sandbox e binding Docker em `127.0.0.1:8080`.
- Evitar reutilizacao de imagem Docker antiga em validacoes futuras; usar build explicito quando houver alteracao de codigo.
- Manter `APP_BOOTSTRAP_ADMIN_ENABLED=false` apos bootstrap.

### Proximo prompt recomendado

Atue como Tech Lead Senior e Backend Engineer Senior. Investigue por que, em profile `prod` com PostgreSQL real, `POST /auth/login` retorna `401 Credenciais invalidas` mesmo quando o usuario ADMIN existe, esta ativo e o hash BCrypt salvo confere com a senha operacional. Nao altere contrato, nao relaxe seguranca e nao imprima segredos. Priorize reproduzir com teste de integracao usando PostgreSQL/Testcontainers ou profile prod-like, corrigir a causa raiz e entao reexecutar `scripts/smoke-test-authenticated.sh`.

## Fase 1.2 - Revalidacao runtime local staging apos diagnostico

Data: 2026-06-07

Ambiente utilizado:

- Docker Compose `docker-compose.staging.yml`;
- projeto Docker isolado `extreme-gym-codex-runtime`;
- profile Spring `prod`;
- PostgreSQL real `postgres:16`;
- porta HTTP local alternativa `19090`, porque `8080` e `18080` estavam ocupadas;
- base local efemera `extreme_staging_codex`;
- credenciais e segredos fornecidos somente por variaveis de ambiente do processo;
- `APP_BOOTSTRAP_ADMIN_ENABLED=true` apenas no bootstrap inicial e depois reiniciado com `APP_BOOTSTRAP_ADMIN_ENABLED=false`.

### Causa raiz

Status: diagnosticado.

Nao foi encontrada falha na logica Java de autenticacao.

Foi adicionado teste de integracao cobrindo o fluxo real:

- ADMIN criado via `AdminUserInitializer`;
- login por `username`;
- login por alias `email` com alias `senha`;
- retorno de JWT, role `ADMIN` e ausencia de `passwordHash` no contrato.

Esse teste passou, e a revalidacao HTTP real tambem passou apos rebuild explicito da imagem e execucao com variaveis corretas. A falha anterior foi classificada como operacional/runtime, associada a risco ja observado de imagem Docker antiga, conflitos de porta e/ou divergencia de variaveis usadas na execucao anterior.

### Correcao aplicada

Status: aplicado.

- `docker-compose.staging.yml` propaga `AUTH_MAX_FAILED_ATTEMPTS` e `AUTH_LOCK_DURATION_MINUTES`;
- `AuthControllerIntegrationTest` passou a cobrir login do ADMIN criado por bootstrap;
- staging local foi reconstruido com imagem nova;
- staging foi reiniciado com bootstrap desligado apos provisionamento.

Nao houve relaxamento de seguranca, alteracao de contrato HTTP, atalho de login ou exposicao de segredo.

### Resultado do compose/staging

Status: aprovado.

Evidencias:

- `docker compose -f docker-compose.staging.yml config --quiet` passou;
- build Docker executou testes durante empacotamento com `169` testes, `0` falhas;
- Postgres ficou `healthy`;
- app ficou `healthy`;
- API root respondeu `200`;
- Flyway validou e aplicou 3 migrations;
- schema chegou em `v3`;
- Swagger respondeu `401` sem JWT no profile `prod`;
- logs revisados nao exibiram senha, JWT completo ou API key.

Observacao operacional:

- `8080` e `18080` estavam ocupadas; a validacao HTTP real foi executada em `19090`.

### Provisionamento e login ADMIN

Status: aprovado.

Evidencias:

- bootstrap criou usuario ADMIN inicial;
- senha nao apareceu nos logs;
- login ADMIN por HTTP real respondeu `200`;
- JWT foi validado apenas com mascara;
- apos reiniciar com `APP_BOOTSTRAP_ADMIN_ENABLED=false`, login ADMIN continuou respondendo `200`.

### Smoke autenticado real

Status: aprovado.

Executado contra `http://localhost:19090` com `SMOKE_CREATE_TEST_DATA=true`.

Resultado:

- API root passou;
- login ADMIN passou;
- rotas protegidas basicas passaram;
- criacao de dispositivo passou;
- API key foi capturada somente em memoria;
- listagem de dispositivos nao expos API key nem hash;
- heartbeat tecnico passou;
- heartbeat sem API key foi rejeitado;
- heartbeat com API key invalida foi rejeitado;
- heartbeat com JWT humano sem API key tecnica foi rejeitado;
- snapshot passou;
- snapshot nao expos PII/biometria;
- snapshot sem API key foi rejeitado;
- snapshot com API key invalida foi rejeitado;
- snapshot com JWT humano sem API key tecnica foi rejeitado;
- validacao de credencial inexistente bloqueou e gerou evento;
- sync lote passou;
- contadores de recebidos/criados/duplicados/rejeitados foram coerentes;
- sync lote sem API key e com API key invalida foi rejeitado;
- massa opcional criou aluno, plano, matricula, pagamento e credencial;
- credencial ativa liberou acesso e gerou evento;
- credencial revogada bloqueou acesso e gerou evento.

### Banco apos smoke

Status: aprovado.

Evidencias seguras:

- 1 dispositivo criado;
- 1 `apiKeyHash` presente;
- hash em formato BCrypt;
- 1 dispositivo com `ultimaComunicacaoEm` preenchido;
- 5 eventos registrados com 5 `idempotencyKey` distintos;
- credencial do smoke ficou `REVOGADA` apos fluxo completo;
- Flyway com versoes 1, 2 e 3 aplicadas com sucesso;
- tabela `dispositivos_acesso` possui apenas coluna `api_key_hash` relacionada a API key, sem coluna plaintext.

### Comandos executados

- `./mvnw -Dtest=AuthControllerIntegrationTest test`: passou com 14 testes;
- `docker compose -p extreme-gym-codex-runtime -f docker-compose.staging.yml config --quiet`: passou;
- `docker compose -p extreme-gym-codex-runtime -f docker-compose.staging.yml up -d --build`: imagem buildada; primeira subida bloqueada por porta `8080`;
- `docker compose -p extreme-gym-codex-runtime -f docker-compose.staging.yml up -d`: passou em `APP_PORT=19090`;
- `scripts/smoke-test-authenticated.sh`: passou com `SMOKE_CREATE_TEST_DATA=true`;
- `bash -n scripts/smoke-test-authenticated.sh`: passou;
- `./mvnw test`: passou com 169 testes, 0 falhas, 0 erros;
- `git diff --check`: passou;
- `git status -sb`: executado.

### Decisao Fase 1.2 revalidada

GO condicionado para piloto tecnico.

Justificativa:

O backend foi validado por HTTP real em profile `prod`, com PostgreSQL real, Flyway, ADMIN bootstrapado, login funcional, dispositivo criado, API key tecnica funcionando, heartbeat, snapshot, validacao por dispositivo, sync lote, idempotencia e testes negativos de seguranca.

Condicao:

Este GO e para piloto tecnico backend/gateway por contrato HTTP. Validacoes de hardware fisico, Face ID e processo gateway externo seguem fora do escopo desta rodada.

### Riscos restantes

- Executar a mesma validacao em staging remoto real com segredos de cofre operacional.
- Garantir que a imagem de staging sempre seja rebuildada ou versionada antes de smoke.
- Manter `APP_BOOTSTRAP_ADMIN_ENABLED=false` apos provisionamento.
- O processo gateway externo nao foi validado porque `GATEWAY_BASE_URL` nao foi informado.
- Portas locais podem conflitar; registrar a porta efetiva usada em cada rodada.

### Proximo prompt recomendado

Atue como Tech Lead Senior, QA Engineer e DevOps Engineer. Execute a mesma validacao em staging remoto real com segredos vindos de cofre operacional, sem commitar `.env`, garantindo imagem versionada, `APP_BOOTSTRAP_ADMIN_ENABLED=false` apos provisionamento e smoke autenticado com `SMOKE_CREATE_TEST_DATA=true`. Se `GATEWAY_BASE_URL` estiver disponivel, incluir health/status do gateway externo e comandos administrativos de heartbeat/snapshot refresh.
