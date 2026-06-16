# Prompt senior - Fase 1.2 runtime prod-like e desbloqueio do gateway

Atue como Tech Lead Senior, Backend Engineer Senior, QA Engineer e DevOps Engineer no projeto Extreme Gym API.

Trabalhe com postura de diagnostico de causa raiz: primeiro confirme o estado real do projeto, depois corrija apenas o necessario, valide por testes automatizados e por HTTP real, e registre evidencias sem expor segredos.

## Contexto do projeto

O projeto e uma API Java 21 com Spring Boot 3.5.14, Maven, Spring Security/JWT, Flyway e PostgreSQL. A arquitetura segue camadas simples (`controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception`) e nao deve receber abstracoes novas sem necessidade real.

O contrato backend minimo de controle de acesso ja existe:

- `DispositivoAcesso`, `CredencialAcesso` e `EventoAcesso`;
- migration `V3__create_access_control_contract.sql`;
- endpoints administrativos de dispositivos e credenciais;
- endpoints tecnicos de heartbeat, snapshot autorizado, validacao online e sincronizacao de eventos;
- autenticacao tecnica por `X-Device-Api-Key`;
- API key retornada uma unica vez e persistida apenas como hash BCrypt;
- snapshot sem PII/biometria;
- eventos com idempotencia por `idempotencyKey`;
- RBAC em Spring Security;
- smoke autenticado em `scripts/smoke-test-authenticated.sh`.

A validacao runtime local staging da Fase 1.2 ja foi tentada em 2026-06-07 e terminou em `NO-GO`: o compose staging subiu com PostgreSQL real e Flyway aplicou migrations, mas `POST /auth/login` retornou `401 Credenciais invalidas` para o ADMIN bootstrapado, mesmo com usuario ativo e hash BCrypt aparentemente coerente. Sem login ADMIN, nao e correto provisionar dispositivo nem aprovar heartbeat, snapshot, validacao por dispositivo ou sync lote.

## Objetivo desta etapa

Desbloquear a validacao runtime real do gateway.

O objetivo primario e investigar e corrigir a causa raiz da falha de login ADMIN em profile `prod`/prod-like com PostgreSQL real. Depois da correcao, reexecutar a validacao staging e o smoke autenticado completo por HTTP real.

Nao transforme falha em sucesso. Se login, dispositivo, API key, heartbeat, snapshot ou sync falharem, registre `NO-GO` com a causa.

## Fora do escopo

- Nao integrar hardware fisico.
- Nao implementar Face ID, webcam, foto, template ou matching biometrico.
- Nao alterar contrato HTTP para contornar o problema.
- Nao relaxar seguranca, RBAC, JWT ou validacao de API key.
- Nao criar gateway fake.
- Nao alterar frontend.
- Nao commitar `.env` real, senha, JWT, API key ou hash reutilizavel.

## Leituras obrigatorias

1. `README.md`
2. `docs/ARCHITECTURE.md`
3. `docs/access-control-backend-contract.md`
4. `docs/runbooks/provisioning-staging.md`
5. `docs/phase-1-pilot-go-no-go-report.md`
6. `docker-compose.staging.yml`
7. `.env.example`
8. `scripts/smoke-test-authenticated.sh`
9. `src/main/java/com/extreme/gym/service/AuthService.java`
10. `src/main/java/com/extreme/gym/dto/auth/LoginRequest.java`
11. `src/main/java/com/extreme/gym/repository/UsuarioRepository.java`
12. `src/main/java/com/extreme/gym/config/AdminUserInitializer.java`
13. `src/main/java/com/extreme/gym/security/SecurityConfig.java`
14. `src/main/java/com/extreme/gym/security/LoginAttemptService.java`
15. `src/main/resources/application.properties`
16. `src/main/resources/application-prod.properties`

## Guardrails de seguranca

- Mascarar JWT e API key em qualquer output.
- Nunca imprimir senha completa, JWT completo, API key completa ou hash sensivel.
- Nunca salvar segredos em docs, scripts, `.env` versionado ou relatorio.
- Usar `APP_BOOTSTRAP_ADMIN_ENABLED=true` somente para bootstrap controlado.
- Desligar `APP_BOOTSTRAP_ADMIN_ENABLED=false` apos bootstrap.
- Manter `AUTH_REGISTRATION_ENABLED=false` em staging/prod.
- Garantir que JWT humano nao autentica rotas tecnicas de dispositivo.
- Usar banco staging/local isolado, nunca producao real.

## Plano de execucao esperado

### 1. Reconhecimento

Antes de editar, levantar:

- `git status -sb`;
- diff existente dos arquivos ja alterados;
- versao atual do prompt, smoke, compose e relatorio Go/No-Go;
- configuracao efetiva de `application.properties` e `application-prod.properties`;
- fluxo real de login entre `LoginRequest`, `AuthController`, `AuthService`, `UsuarioRepository`, `PasswordEncoder` e rate limit.

Preserve alteracoes existentes do workspace. Se encontrar mudancas nao relacionadas, nao reverta.

### 2. Reproducao da falha

Reproduzir a falha sem expor segredos:

- subir staging local com `docker-compose.staging.yml` e PostgreSQL real;
- confirmar Postgres/app healthy;
- confirmar Flyway aplicado;
- confirmar que ADMIN foi bootstrapado e esta ativo;
- chamar `POST /auth/login` por HTTP real usando `username` e, se necessario, alias `email`;
- diferenciar erro de credencial, payload, binding JSON, lookup no banco, BCrypt, rate limit e imagem Docker antiga.

Se a falha nao reproduzir, registre a diferenca de ambiente e prossiga para smoke completo.

### 3. Diagnostico tecnico

Investigar prioritariamente:

- se `LoginRequest` recebe o campo correto (`username`, alias `email`, `password`, alias `senha`);
- se `UsuarioRepository.findByEmailOrUsername` encontra usuario com case-insensitive real em PostgreSQL;
- se `Usuario.getAtivo()` corresponde ao valor persistido;
- se `passwordEncoder.matches` usa o mesmo BCrypt do bootstrap;
- se existe divergencia entre variaveis `ADMIN_*` e `APP_BOOTSTRAP_ADMIN_*`;
- se o rate limit esta mascarando a causa principal;
- se container antigo foi reutilizado sem rebuild;
- se profile `prod` esta lendo propriedades esperadas.

Preferir teste automatizado prod-like ou Testcontainers se houver estrutura adequada. Se Testcontainers nao existir, criar teste focado com o minimo de dependencia possivel, respeitando o padrao do projeto.

### 4. Correcao

Corrigir somente a causa raiz encontrada.

Regras:

- manter autenticacao segura;
- manter registro publico desabilitado por padrao;
- nao enfraquecer validacoes;
- nao retornar diagnosticos sensiveis ao cliente;
- nao adicionar logs com segredo;
- preservar contrato existente do smoke, salvo se o bug estiver no proprio script.

### 5. Validacao automatizada

Executar obrigatoriamente:

```bash
bash -n scripts/smoke-test-authenticated.sh
./mvnw test
git diff --check
```

Se alterar arquivos de frontend por acidente, rever escopo antes de prosseguir. Frontend nao faz parte desta etapa.

### 6. Validacao runtime staging

Executar com variaveis locais/efemeras:

```bash
docker compose -f docker-compose.staging.yml config
docker compose -f docker-compose.staging.yml up -d --build
docker compose -f docker-compose.staging.yml ps
```

Validar:

- Postgres healthy;
- app healthy;
- API root responde 200;
- Flyway aplicou migrations;
- tabelas de usuarios, dispositivos, credenciais e eventos existem;
- Swagger protegido/desabilitado no profile `prod`;
- logs sem senha, JWT, API key ou hash sensivel.

### 7. Smoke autenticado completo

Executar:

```bash
API_BASE_URL=<url-local> \
ADMIN_LOGIN=<login-operacional> \
ADMIN_PASSWORD=<senha-operacional> \
DEVICE_NAME=<nome-dispositivo> \
DEVICE_TYPE=GATEWAY \
DEVICE_MODE=HIBRIDO \
SMOKE_CREATE_TEST_DATA=true \
scripts/smoke-test-authenticated.sh
```

O smoke deve comprovar:

- API root;
- login ADMIN emitindo JWT mascarado;
- rotas protegidas basicas com JWT;
- criacao de dispositivo por ADMIN;
- API key capturada somente em memoria;
- listagem de dispositivos sem API key/hash;
- heartbeat com `X-Device-Api-Key`;
- rejeicao de heartbeat sem API key;
- rejeicao de heartbeat com API key invalida;
- rejeicao de JWT humano como substituto de API key tecnica;
- snapshot com API key valida;
- snapshot sem PII/biometria;
- rejeicoes de snapshot sem API key/invalida;
- validacao por dispositivo bloqueando credencial inexistente e gerando evento;
- sync lote com contadores coerentes;
- idempotencia por `idempotencyKey`;
- massa opcional com aluno, plano, matricula, pagamento, credencial ativa e credencial revogada.

### 8. Banco apos smoke

Validar de forma segura:

- dispositivo criado;
- `apiKeyHash` existe;
- API key plaintext nao foi persistida;
- `ultimaComunicacaoEm` foi atualizado;
- eventos foram registrados;
- idempotencyKey nao duplicou evento;
- credencial ativa libera e revogada bloqueia, se `SMOKE_CREATE_TEST_DATA=true`.

Nao imprimir segredo nem salvar dump sensivel.

### 9. Atualizacao do Go/No-Go

Atualizar `docs/phase-1-pilot-go-no-go-report.md` com:

- causa raiz encontrada;
- correcao aplicada;
- resultado dos testes automatizados;
- resultado do compose staging;
- resultado do login ADMIN;
- resultado da criacao de dispositivo;
- resultado de heartbeat;
- resultado de snapshot;
- resultado de validacao por dispositivo;
- resultado de sync lote;
- confirmacao de nao vazamento de segredo;
- riscos restantes;
- decisao final.

## Criterios de GO condicionado

Marcar `GO condicionado para piloto tecnico` somente se:

- login ADMIN funciona por HTTP real;
- dispositivo e criado por ADMIN;
- API key e retornada uma unica vez e nao vaza em listagem/log/output;
- heartbeat passa com API key valida;
- snapshot passa com API key valida e sem PII/biometria;
- sync lote passa com contadores coerentes;
- API key ausente/invalida e rejeitada;
- JWT humano nao substitui API key tecnica;
- `./mvnw test` passa;
- `git diff --check` passa;
- nao ha P0/P1 aberto.

## Criterios de NO-GO

Manter `NO-GO` se:

- app nao sobe;
- login ADMIN falha;
- dispositivo nao e criado;
- heartbeat, snapshot ou sync retornam 401/403 indevido com API key valida;
- API key, JWT, senha ou hash sensivel vazam;
- JWT humano e aceito indevidamente em endpoint tecnico;
- smoke falha em fluxo principal;
- nao houve validacao HTTP real.

## Formato da resposta final

Retorne em portugues:

1. Ambiente runtime utilizado.
2. Causa raiz encontrada.
3. Correcao aplicada.
4. Resultado do compose/staging.
5. Resultado do login ADMIN.
6. Resultado da criacao de dispositivo.
7. Resultado de heartbeat.
8. Resultado de snapshot.
9. Resultado de validacao por dispositivo.
10. Resultado de sync lote.
11. Resultado dos testes negativos de seguranca.
12. Confirmacao de nao vazamento de segredo.
13. Arquivos alterados.
14. Resultado dos comandos.
15. Decisao Go/No-Go atualizada.
16. Riscos restantes.
17. Proximo prompt recomendado, se ainda houver bloqueio.
