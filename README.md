# Extreme Gym API

API REST para gestao de academia, desenvolvida com Java 21, Spring Boot e PostgreSQL.

## Objetivo

Construir uma API simples, limpa e evolutiva para organizar operacoes essenciais de uma academia, como cadastro de alunos, planos, matriculas, pagamentos e check-ins.

O projeto foi estruturado para evoluir de forma incremental, com separacao clara entre controller, service, repository, DTOs, validacoes, tratamento de erros e testes automatizados.

## Status atual

O projeto possui os modulos de Alunos, Planos, Matriculas, Pagamentos, Check-ins e Validacao de Acesso implementados.

Ja esta disponivel:

- Aplicacao Spring Boot rodando em `localhost:8080`.
- PostgreSQL local via Docker Compose.
- Profile de teste com H2.
- CRUD basico de alunos.
- Validacoes de entrada com Bean Validation.
- Tratamento padronizado de erros.
- DTOs para entrada e saida.
- Testes unitarios do service com JUnit e Mockito.
- Endpoint raiz `GET /` para verificar se a API esta respondendo.
- CRUD de planos com validacoes, regra de nome unico e remocao logica.
- Gerenciamento de matriculas conectando aluno e plano.
- Gerenciamento de pagamentos confirmados vinculados a matriculas.
- Registro de check-ins permitidos ou bloqueados, mantendo historico da tentativa.
- Validacao de acesso sem registro de check-in, preparada para integracoes futuras.
- Swagger/OpenAPI para documentacao interativa da API.
- Dockerfile para empacotar e executar a aplicacao via container.
- Testes unitarios de services e testes de integracao/controller com MockMvc.

Ultima validacao conhecida: `118` testes executados, com `0` falhas, `0` erros e build finalizado com sucesso.

## Fluxo completo do MVP

O fluxo operacional implementado no MVP e:

1. Cadastrar o aluno em `POST /alunos`.
2. Cadastrar um plano ativo em `POST /planos`.
3. Criar a matricula em `POST /matriculas`, vinculando aluno e plano.
4. Registrar o pagamento confirmado em `POST /pagamentos`.
5. Validar acesso em `POST /acessos/validar`, quando a necessidade for apenas saber se o aluno pode entrar.
6. Registrar a tentativa real de entrada em `POST /checkins`, mantendo historico de acesso permitido ou bloqueado.

## Ordem das regras de acesso

A decisao de acesso segue esta ordem:

1. Aluno deve existir.
2. Aluno nao pode estar `BLOQUEADO`.
3. Aluno nao pode estar `CANCELADO`.
4. Aluno nao pode estar `INADIMPLENTE`.
5. Aluno deve possuir matricula `ATIVA`.
6. Matricula ativa nao pode estar vencida pela data final.
7. Matricula deve possuir pagamento `PAGO`.

Quando todas as regras passam, o acesso e liberado. Quando alguma regra falha, a resposta retorna acesso bloqueado com motivo claro.

## Validacao de Acesso x Check-in

`POST /acessos/validar` e uma consulta somente leitura. Ela informa se o aluno pode acessar a academia naquele momento e nao registra tentativa, nao cria check-in e nao altera dados no banco.

`POST /checkins` representa a tentativa real de entrada. Ele reutiliza a mesma decisao de acesso e grava o historico da tentativa, tanto quando o acesso e permitido quanto quando e bloqueado.

## Stack utilizada

- Java 21
- Spring Boot 3.5.14
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL via Docker
- Docker
- H2 para testes
- Bean Validation
- Lombok
- JUnit
- Mockito
- springdoc-openapi

## Qualidade, testes e documentacao

O Extreme Gym API foi organizado como um MVP backend Java/Spring Boot com foco em clareza de arquitetura, regras de negocio testaveis e documentacao objetiva para avaliacao tecnica.

- Separacao em camadas: Controller, Service, Repository, DTO e Entity.
- Testes unitarios cobrindo services e principais regras de negocio.
- Testes de integracao/controller com MockMvc para Alunos, Planos, Matriculas, Pagamentos, Check-ins e Validacao de Acesso.
- Suite automatizada validada com `118` testes passando: `0` falhas, `0` erros e `0` ignorados.
- Profile de teste com H2, mantendo os testes independentes do PostgreSQL local.
- Ambiente de desenvolvimento usando PostgreSQL via Docker Compose.
- Empacotamento da aplicacao com Dockerfile multi-stage e Java 21.
- Swagger/OpenAPI disponivel como documentacao interativa para consulta e teste dos endpoints.

Os detalhes de arquitetura, contrato da API, setup local e regras de negocio ficam concentrados na pasta [`docs`](docs), mantendo este README como visao geral do projeto.

## Funcionalidades implementadas

### Alunos

- Cadastro de aluno.
- Listagem de alunos.
- Busca de aluno por id.
- Atualizacao de aluno.
- Remocao de aluno.
- Validacao de nome, email e telefone.
- Bloqueio de email duplicado no cadastro.
- Bloqueio de email duplicado na atualizacao, permitindo que o aluno mantenha o proprio email.
- Status inicial do aluno como `ATIVO`.
- Respostas de erro padronizadas.
- Testes unitarios para os principais cenarios de service.

### Planos

- Cadastro de plano.
- Listagem de planos.
- Busca de plano por id.
- Atualizacao de plano.
- Desativacao de plano por remocao logica.
- Validacao de nome, valor mensal e duracao em dias.
- Bloqueio de nome duplicado no cadastro.
- Bloqueio de nome duplicado na atualizacao, permitindo que o plano mantenha o proprio nome.
- Status inicial do plano como ativo.
- Data de cadastro preenchida automaticamente.
- Testes unitarios para os principais cenarios de service.

### Matriculas

- Criacao de matricula vinculando aluno e plano.
- Listagem de matriculas.
- Busca de matricula por id.
- Cancelamento de matricula por alteracao de status.
- Validacao de aluno existente.
- Validacao de plano existente e ativo.
- Bloqueio de mais de uma matricula `ATIVA` para o mesmo aluno.
- Calculo automatico da data final com base na duracao do plano.
- Status inicial da matricula como `ATIVA`.
- Data de cadastro preenchida automaticamente.
- Testes unitarios para os principais cenarios de service.

### Pagamentos

- Registro de pagamento vinculado a matricula.
- Listagem de pagamentos.
- Busca de pagamento por id.
- Listagem de pagamentos por matricula.
- Cancelamento de pagamento por alteracao de status.
- Validacao de matricula existente e ativa.
- Bloqueio de pagamento para matricula `CANCELADA`.
- Bloqueio de pagamento para matricula `VENCIDA` nesta versao inicial.
- Bloqueio de pagamento `PAGO` duplicado para a mesma matricula.
- Status inicial do pagamento registrado como `PAGO`.
- Data de pagamento e data de cadastro preenchidas automaticamente.
- Testes unitarios para os principais cenarios de service.

### Check-ins

- Registro de tentativa de check-in vinculada a aluno.
- Listagem de check-ins.
- Busca de check-in por id.
- Listagem de check-ins por aluno.
- Bloqueio de entrada para aluno `BLOQUEADO`, `CANCELADO` ou `INADIMPLENTE`.
- Validacao de matricula `ATIVA`.
- Bloqueio de entrada quando a matricula esta vencida pela data final.
- Validacao de pagamento `PAGO` para liberar o acesso.
- Registro da tentativa mesmo quando o acesso e bloqueado.
- Motivo claro para acesso permitido ou bloqueado.
- Resposta sem expor entidades completas de aluno ou matricula.
- Testes unitarios para os principais cenarios de service.

### Validacao de Acesso

- Validacao se um aluno pode acessar a academia sem registrar check-in.
- Bloqueio de acesso para aluno `BLOQUEADO`, `CANCELADO` ou `INADIMPLENTE`.
- Exigencia de matricula `ATIVA`, nao vencida e com pagamento `PAGO`.
- Retorno de motivo claro para acesso liberado ou bloqueado.
- Resposta sem expor entidades completas de aluno ou matricula.
- Service somente leitura, sem alteracao de banco.
- Preparacao para futuras integracoes com catraca, QR Code ou Face ID.
- Nenhuma integracao fisica foi implementada nesta fase.
- Testes unitarios para os principais cenarios de service.

## Endpoints disponiveis

| Metodo | Path | Objetivo |
| --- | --- | --- |
| `GET` | `/` | Verificar se a API esta rodando |
| `POST` | `/alunos` | Cadastrar aluno |
| `GET` | `/alunos` | Listar alunos |
| `GET` | `/alunos/{id}` | Buscar aluno por id |
| `PUT` | `/alunos/{id}` | Atualizar aluno |
| `DELETE` | `/alunos/{id}` | Remover aluno |
| `POST` | `/planos` | Cadastrar plano |
| `GET` | `/planos` | Listar planos |
| `GET` | `/planos/{id}` | Buscar plano por id |
| `PUT` | `/planos/{id}` | Atualizar plano |
| `DELETE` | `/planos/{id}` | Desativar plano |
| `POST` | `/matriculas` | Criar matricula |
| `GET` | `/matriculas` | Listar matriculas |
| `GET` | `/matriculas/{id}` | Buscar matricula por id |
| `PATCH` | `/matriculas/{id}/cancelar` | Cancelar matricula |
| `POST` | `/pagamentos` | Registrar pagamento |
| `GET` | `/pagamentos` | Listar pagamentos |
| `GET` | `/pagamentos/{id}` | Buscar pagamento por id |
| `GET` | `/pagamentos/matricula/{matriculaId}` | Listar pagamentos por matricula |
| `PATCH` | `/pagamentos/{id}/cancelar` | Cancelar pagamento |
| `POST` | `/checkins` | Registrar tentativa de check-in |
| `GET` | `/checkins` | Listar check-ins |
| `GET` | `/checkins/{id}` | Buscar check-in por id |
| `GET` | `/checkins/aluno/{alunoId}` | Listar check-ins por aluno |
| `POST` | `/acessos/validar` | Validar se o aluno pode acessar a academia |

Contrato detalhado: [docs/API_CONTRACT.md](docs/API_CONTRACT.md)

## Documentacao da API com Swagger

A API expoe documentacao automatica com Swagger/OpenAPI usando springdoc-openapi.

Com a aplicacao rodando em `localhost:8080`, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Swagger UI alternativo: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

O Swagger permite visualizar e testar os endpoints da API pelo navegador. Ele e apenas documentacao interativa da API; nao adiciona autenticacao, autorizacao ou deploy.

## Como rodar localmente

Entre na raiz real do projeto, onde estao o `pom.xml` e o `docker-compose.yml`.

No WSL:

```bash
cd /mnt/c/Users/ednal/Documents/Projetos/extreme-gym-api/extreme-gym-api
```

No PowerShell:

```powershell
cd C:\Users\ednal\Documents\Projetos\extreme-gym-api\extreme-gym-api
```

## Subir ambiente com Docker Compose

```bash
docker compose up -d --build
```

Verificar se os containers estao rodando:

```bash
docker compose ps
```

Os containers esperados sao `extreme-postgres` e `extreme-gym-api`. Os detalhes operacionais ficam em [docs/SETUP.md](docs/SETUP.md).

## Rodar a aplicacao

No WSL:

```bash
./mvnw spring-boot:run
```

No PowerShell:

```powershell
.\mvnw spring-boot:run
```

Quando aparecer `Tomcat started on port 8080`, a aplicacao esta escutando requisicoes HTTP em `localhost:8080`.

Para validar rapidamente:

```bash
curl http://localhost:8080/
```

Resposta esperada:

```json
{
  "message": "Extreme Gym API is running"
}
```

Tambem e possivel gerar e executar apenas a imagem Docker da aplicacao. Os comandos completos ficam em [docs/SETUP.md](docs/SETUP.md).

## Rodar os testes

No WSL:

```bash
./mvnw test
```

No PowerShell:

```powershell
.\mvnw test
```

Na ultima validacao, a suite passou com 118 testes e 0 falhas.

## Documentacao adicional

- [Escopo do projeto](PROJECT_SCOPE.md)
- [Roadmap](docs/ROADMAP.md)
- [Setup local](docs/SETUP.md)
- [Contrato da API](docs/API_CONTRACT.md)
- [Regras de negocio](docs/BUSINESS_RULES.md)
- [Arquitetura](docs/ARCHITECTURE.md)

## Fora do escopo atual

Ainda nao foram implementados:

- Autenticacao JWT.
- Flyway.
- Deploy.
- Frontend.
- Integracao com catraca, QR Code, Face ID ou controle fisico de acesso.

Esses itens permanecem como evolucoes futuras.

## Proximo passo

O proximo passo tecnico recomendado e planejar uma fase futura de autenticacao, deploy ou notificacoes, mantendo integracoes fisicas fora do MVP atual ate haver necessidade real.
