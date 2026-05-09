# Extreme Gym API

API REST para gestao de academia, desenvolvida com Java 21, Spring Boot e PostgreSQL.

## Objetivo

Construir uma API simples, limpa e evolutiva para organizar operacoes essenciais de uma academia, como cadastro de alunos, planos, matriculas, pagamentos e check-ins.

## Problema que resolve

Academias pequenas e medias precisam centralizar informacoes basicas de alunos e operacoes recorrentes sem depender de controles manuais, planilhas soltas ou processos dificeis de acompanhar.

O projeto busca criar uma base tecnica profissional para esse dominio, com organizacao clara, infraestrutura local reproduzivel e evolucao incremental.

## Stack utilizada

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA
- PostgreSQL
- Bean Validation
- Lombok
- Docker
- Maven Wrapper

## Funcionalidades planejadas

- Cadastro de alunos
- Cadastro de planos
- Matriculas de alunos em planos
- Controle de pagamentos
- Registro de check-ins
- Validacoes de entrada
- Tratamento padronizado de erros
- Documentacao da API com Swagger
- Autenticacao futura com JWT

## Status atual do projeto

O projeto esta na fase inicial de setup e infraestrutura.

Ja existe uma aplicacao Spring Boot criada pelo Spring Initializr, com Maven Wrapper funcionando, dependencias principais configuradas e conexao local planejada com PostgreSQL via Docker Compose.

Ainda nao existem entidades, controllers, services, DTOs, autenticacao ou regras de negocio implementadas.

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

Quando aparecer `Tomcat started on port 8080`, significa que a aplicacao subiu corretamente e esta escutando requisicoes HTTP na porta `8080`.

## Rodar os testes

No WSL:

```bash
./mvnw test
```

No PowerShell:

```powershell
.\mvnw test
```

O teste inicial valida apenas o carregamento basico do contexto da aplicacao. Testes de regras de negocio serao adicionados quando o dominio comecar a ser implementado.

## Pendencias conhecidas de ambiente

- Se `docker compose up -d` retornar erro no WSL, verifique a integracao do Docker Desktop com o WSL ou execute o comando pelo PowerShell.
- Se a aplicacao retornar `Connection refused` em `localhost:5432`, significa que o PostgreSQL nao esta rodando ou a porta `5432` nao esta disponivel.

## Regras principais do MVP

- O MVP deve comecar pelo cadastro de alunos.
- O cadastro de planos deve ser implementado antes de matriculas.
- Matriculas devem associar alunos a planos existentes.
- Pagamentos devem estar relacionados a matriculas.
- Check-ins devem ser registrados apenas depois da base de alunos, planos e matriculas existir.
- Autenticacao, Swagger e testes serao adicionados em fases posteriores.
- O projeto deve evoluir sem overengineering, mantendo clareza e baixo acoplamento.

## Proximo passo

O proximo passo tecnico e garantir o PostgreSQL rodando localmente pelo Docker Compose e, em seguida, iniciar o CRUD de alunos.
