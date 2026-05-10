# Extreme Gym API

API REST para gestao de academia, desenvolvida com Java 21, Spring Boot e PostgreSQL.

## Objetivo

Construir uma API simples, limpa e evolutiva para organizar operacoes essenciais de uma academia, como cadastro de alunos, planos, matriculas, pagamentos e check-ins.

O projeto foi estruturado para evoluir de forma incremental, com separacao clara entre controller, service, repository, DTOs, validacoes, tratamento de erros e testes automatizados.

## Status atual

O projeto possui os modulos de Alunos, Planos e Matriculas implementados.

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

Proximo modulo planejado: Pagamentos.

## Stack utilizada

- Java 21
- Spring Boot 3.5.14
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL via Docker
- H2 para testes
- Bean Validation
- Lombok
- JUnit
- Mockito

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

Contrato detalhado: [docs/API_CONTRACT.md](docs/API_CONTRACT.md)

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

## Subir o banco de dados

```bash
docker compose up -d
```

Verificar se o container esta rodando:

```bash
docker ps
```

O container esperado e `extreme-postgres`, usando a porta local `5432`.

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

## Rodar os testes

No WSL:

```bash
./mvnw test
```

No PowerShell:

```powershell
.\mvnw test
```

Na ultima validacao, a suite passou com 31 testes e 0 falhas.

## Documentacao adicional

- [Escopo do projeto](PROJECT_SCOPE.md)
- [Roadmap](docs/ROADMAP.md)
- [Setup local](docs/SETUP.md)
- [Contrato da API](docs/API_CONTRACT.md)
- [Regras de negocio](docs/BUSINESS_RULES.md)
- [Arquitetura](docs/ARCHITECTURE.md)

## Fora do escopo atual

Ainda nao foram implementados:

- Pagamentos.
- Check-ins.
- Autenticacao JWT.
- Swagger.
- Flyway.
- Dockerfile.
- Deploy.
- Frontend.
- Integracao com catraca, QR Code, Face ID ou controle fisico de acesso.

Esses itens permanecem como evolucoes futuras.

## Proximo passo

O proximo passo tecnico e implementar o modulo de Pagamentos, associado as matriculas ja criadas.
