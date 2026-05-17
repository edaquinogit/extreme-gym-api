# Software Design Document - Extreme Gym API

## 1. Contexto e Objetivo

Extreme Gym API e uma API REST para gestao de academia, construida em Java 21, Spring Boot e PostgreSQL.

O objetivo deste documento e orientar a evolucao profissional do backend sem reescrever o MVP do zero. A aplicacao deve crescer como monolito modular, mantendo simplicidade, clareza, testes automatizados e contratos HTTP estaveis para um futuro frontend em React + TypeScript.

## 2. Escopo Atual

O MVP atual possui os seguintes modulos funcionais:

- Alunos.
- Planos.
- Matriculas.
- Pagamentos.
- Check-ins.
- Validacao de acesso.

A estrutura atual esta organizada por camadas globais:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`
- `config`

Essa estrutura e adequada para o MVP, mas deve evoluir gradualmente para reduzir acoplamento entre dominios conforme novas regras forem adicionadas.

## 3. Escopo Fora Desta Etapa

Esta etapa inicial nao inclui:

- Migracao completa para arquitetura modular por dominio.
- Microsservicos.
- Autenticacao completa com JWT.
- Criacao imediata de frontend.
- Integracoes reais com catraca, QR Code, Face ID ou WhatsApp.
- Refatoracao ampla de services e entities.
- Alteracoes destrutivas no banco de dados.

## 4. Arquitetura Atual

A arquitetura atual segue camadas tradicionais em Spring Boot.

Controllers recebem requisicoes HTTP, validam DTOs e delegam para services. Services concentram regras de negocio e orquestram repositories. Repositories usam Spring Data JPA. Entities representam tabelas do banco. DTOs protegem os contratos HTTP contra exposicao direta das entities.

Pontos positivos:

- Controllers estao finos.
- DTOs ja existem para entrada e saida.
- Regras principais estao em services.
- Existe tratamento global de excecoes.
- O projeto possui documentacao tecnica em `docs`.
- Existem testes unitarios e testes de controller/integracao.

Pontos de atencao:

- Ainda nao existe seguranca/autenticacao.
- O banco ainda nao e versionado por migrations.
- Listagens retornam listas completas em varios endpoints.
- Regras criticas dependem apenas de validacao em service.
- Services podem crescer demais se novos fluxos forem adicionados sem modularizacao.
- Datas sao obtidas diretamente via `LocalDate.now()` e `LocalDateTime.now()` em pontos de regra.

## 5. Arquitetura Alvo

A arquitetura alvo e um monolito modular por dominio, mantendo Spring Boot e JPA.

Estrutura desejada de forma gradual:

```text
src/main/java/com/extreme/gym
├── aluno
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
├── plano
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
├── matricula
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
├── pagamento
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
├── acesso
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
├── usuario
│   ├── api
│   ├── application
│   ├── domain
│   └── infra
└── shared
    ├── config
    ├── exception
    ├── security
    ├── pagination
    └── util
```

Essa reorganizacao deve ser feita por etapas, preferencialmente quando um modulo receber alteracoes relevantes. O objetivo e evitar uma refatoracao grande sem ganho imediato.

## 6. Modulos do Sistema

### Alunos

Responsavel por cadastro, consulta, atualizacao e estado operacional do aluno.

Evolucoes previstas:

- Filtros por nome, CPF e status.
- Paginacao.
- Historico de alteracoes sensiveis.
- Associacao futura com usuario do tipo `ALUNO`.

### Planos

Responsavel por planos disponiveis para matriculas.

Evolucoes previstas:

- Paginacao e filtros por nome e ativo.
- Regras de inativacao segura.
- Historico de preco, se necessario.

### Matriculas

Responsavel pelo vinculo entre aluno e plano.

Evolucoes previstas:

- Motivo e data de cancelamento.
- Constraint para impedir mais de uma matricula ativa por aluno.
- Filtros por status, aluno, plano e vencimento.

### Pagamentos

Responsavel por registros financeiros vinculados a matriculas.

Evolucoes previstas:

- Competencia de pagamento.
- Bloqueio de pagamento duplicado por competencia.
- Filtros por periodo, status, aluno e matricula.
- Preparacao para inadimplencia real.

### Acessos e Check-ins

Responsavel por validar entrada e registrar tentativas reais.

Evolucoes previstas:

- Perfil `CATRACA`.
- Auditoria de origem da tentativa.
- Filtros por aluno, periodo e autorizado.
- Integracoes futuras com QR Code, catraca e Face ID.

### Usuarios

Modulo ainda nao implementado.

Responsavel por autenticacao, autorizacao e identidade de operadores do sistema.

Perfis previstos:

- `ADMIN`
- `RECEPCAO`
- `PROFESSOR`
- `ALUNO`
- `CATRACA`

## 7. Modelo de Dados

O modelo atual possui entities para:

- `Aluno`
- `Plano`
- `Matricula`
- `Pagamento`
- `CheckIn`

Evolucoes previstas:

- `Usuario`
- `Role` ou enum de perfil persistido.
- Campos de auditoria em entidades sensiveis.
- Campos de cancelamento em matricula e pagamento.
- Indices para consultas frequentes.
- Constraints de integridade para regras criticas.

As migrations devem ser introduzidas com Flyway antes de novas alteracoes estruturais relevantes.

## 8. Regras de Negocio

Regras atuais importantes:

- Email de aluno nao pode ser duplicado.
- Nome de plano nao pode ser duplicado.
- Plano inativo nao pode ser usado em matricula.
- Aluno nao pode ter mais de uma matricula ativa.
- Matricula vencida bloqueia acesso.
- Matricula precisa possuir pagamento pago para liberar acesso.
- Check-in registra tentativas permitidas e bloqueadas.

Regras que precisam ser fortalecidas:

- Garantir matricula ativa unica tambem no banco.
- Modelar competencia de pagamento.
- Bloquear pagamento duplicado por competencia.
- Registrar motivo e data de cancelamento de matricula.
- Evitar exclusao fisica em entidades sensiveis.

## 9. Seguranca e Permissoes

A seguranca alvo usa Spring Security com JWT e RBAC.

A primeira implementacao de seguranca foi adicionada na Etapa 2 com:

- Spring Security usando `SecurityFilterChain`.
- JWT stateless com header `Authorization: Bearer <token>`.
- Senhas armazenadas com BCrypt.
- Entidade `Usuario` com perfil unico.
- Registro publico controlado por `app.auth.registration-enabled`, habilitado para bootstrap em desenvolvimento e desabilitado por padrao em producao.
- Testes de login, bloqueio sem token, acesso autorizado e negacao por role.

Perfis e responsabilidades:

- `ADMIN`: acesso total, gerenciamento de usuarios, planos, relatorios e cancelamentos.
- `RECEPCAO`: cadastro de alunos, matriculas, pagamentos e consultas operacionais.
- `PROFESSOR`: consulta de alunos ativos e check-ins.
- `ALUNO`: consulta futura dos proprios dados.
- `CATRACA`: validacao de acesso e registro de check-in sem permissoes administrativas.

Diretrizes:

- Endpoints administrativos nao devem ser publicos.
- Swagger deve permanecer acessivel em desenvolvimento.
- Rotas de autenticacao devem ser publicas.
- Regras de autorizacao devem ser testadas.

## 10. Estrategia de Paginacao e Filtros

Listagens de alto crescimento devem usar `Pageable`.

Endpoints candidatos:

- `GET /alunos`
- `GET /planos`
- `GET /matriculas`
- `GET /pagamentos`
- `GET /checkins`

Filtros previstos:

- Alunos: nome, CPF e status.
- Planos: nome e ativo.
- Matriculas: status, alunoId, planoId e vencimento.
- Pagamentos: status, alunoId, matriculaId, dataInicio e dataFim.
- Check-ins: alunoId, dataInicio, dataFim e autorizado.

## 11. Tratamento de Erros

O projeto ja possui `GlobalExceptionHandler`.

Padrao desejado:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors`, quando houver erro de validacao

Excecoes previstas:

- `ResourceNotFoundException`
- `BusinessException`
- `DuplicateResourceException`
- `InvalidOperationException`
- `UnauthorizedException`, se necessario
- `ForbiddenException`, se necessario

Conflitos de unicidade devem retornar `409 Conflict` quando representarem duplicidade de recurso.

## 12. Estrategia de Testes

Tipos de teste desejados:

- Testes unitarios de services e regras de negocio.
- Testes de controller com MockMvc.
- Testes de seguranca para token e roles.
- Testes de filtros e paginacao.
- Testes de migrations quando Flyway for introduzido.
- Testes determinísticos para regras temporais usando `Clock`.

Prioridades:

- Garantir que `./mvnw test` execute com sucesso.
- Evitar testes acoplados a detalhes internos desnecessarios.
- Cobrir regras criticas de matricula, pagamento e acesso.

## 13. Roadmap Tecnico

### Etapa 1: Documentacao e decisoes

- Criar SDD.
- Criar ADRs iniciais.
- Registrar riscos e plano incremental.

### Etapa 2: Seguranca

- Adicionar Spring Security.
- Criar modulo inicial de usuarios.
- Implementar JWT.
- Criar RBAC.
- Proteger endpoints.
- Adicionar testes de autenticacao e autorizacao.

### Etapa 3: Banco e migrations

- Adicionar Flyway.
- Criar migrations iniciais.
- Trocar gradualmente `ddl-auto` para `validate`.
- Adicionar constraints e indices.

### Etapa 4: Paginacao e filtros

- Introduzir `Pageable` em listagens principais.
- Criar filtros simples por modulo.
- Atualizar testes e documentacao da API.

### Etapa 5: Regras criticas e dominio

- Fortalecer invariantes.
- Adicionar campos de cancelamento.
- Introduzir `Clock`.
- Reduzir crescimento dos services.

### Etapa 6: Preparacao para frontend

- Revisar contratos.
- Padronizar responses.
- Melhorar Swagger/OpenAPI.
- Documentar consumo da API.

## 14. Riscos Tecnicos

- Reorganizacao radical pode quebrar testes e atrasar features.
- Introduzir seguranca sem testes pode bloquear endpoints indevidamente.
- Flyway precisa considerar o estado atual do banco local.
- Constraints novas podem falhar se ja existirem dados inconsistentes.
- Paginacao muda contratos e precisa ser coordenada com consumidores.
- Regras temporais sem `Clock` tornam testes e bugs de data mais dificeis.

## 15. Decisoes Arquiteturais

Decisoes registradas:

- Monolito modular como arquitetura alvo.
- Flyway como ferramenta de migrations.
- Spring Security + JWT + RBAC como estrategia de seguranca.
- Paginacao obrigatoria em listagens com crescimento.
- `Clock` injetavel para regras temporais.

Os detalhes ficam em `docs/adr`.
