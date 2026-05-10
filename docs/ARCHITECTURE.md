# Architecture

## Visao geral

O Extreme Gym API sera uma aplicacao monolitica modular baseada em Spring Boot, organizada em camadas simples e bem definidas.

A arquitetura inicial prioriza clareza, baixo acoplamento e facilidade de evolucao. O projeto nao deve introduzir abstracoes antes de haver necessidade real.

## Camadas planejadas

### controller

Responsavel por expor endpoints REST, receber requisicoes HTTP, validar entradas e retornar respostas apropriadas.

Controllers nao devem conter regra de negocio. Eles devem delegar o fluxo principal para services.

### service

Responsavel pelas regras de negocio e orquestracao dos casos de uso.

Services devem validar regras do dominio, coordenar operacoes entre repositories e preparar respostas para a camada externa.

### repository

Responsavel pelo acesso a dados usando Spring Data JPA.

Repositories devem representar operacoes de persistencia e consultas, sem conter regra de negocio.

### entity

Responsavel por representar as tabelas e relacionamentos do banco de dados.

Entities devem conter apenas o necessario para persistencia e modelagem do dominio.

### dto

Responsavel por transportar dados entre a API e seus consumidores.

DTOs evitam expor diretamente as entities nos contratos HTTP e permitem validacoes especificas para entrada e saida.

### exception

Responsavel pelo tratamento padronizado de erros.

Essa camada deve concentrar excecoes customizadas e handlers globais para respostas consistentes.

### config

Responsavel por configuracoes tecnicas da aplicacao.

Essa camada deve ser usada apenas quando houver configuracoes reais a centralizar, evitando arquivos vazios ou estruturas antecipadas.

## Responsabilidade de cada camada

- `controller`: contrato HTTP e delegacao.
- `service`: regra de negocio e casos de uso.
- `repository`: persistencia e consultas.
- `entity`: mapeamento JPA e estado persistido.
- `dto`: entrada e saida da API.
- `exception`: erros padronizados.
- `config`: configuracoes tecnicas.

## Principios de Clean Code aplicados

- Nomes claros e orientados ao dominio.
- Metodos pequenos e com responsabilidade unica.
- Separacao entre entrada HTTP, regra de negocio e persistencia.
- Validacao declarativa com Bean Validation sempre que possivel.
- Evitar duplicacao prematura e abstracoes desnecessarias.
- Evoluir a estrutura conforme o dominio surgir.
- Manter controllers finos e services focados.

## Decisoes tecnicas iniciais

- Java 21 como versao base da linguagem.
- Spring Boot 3.5.14 como framework principal.
- PostgreSQL como banco relacional.
- Docker Compose para ambiente local de banco de dados.
- Spring Data JPA para persistencia.
- Bean Validation para validacao de entradas.
- Lombok para reduzir codigo repetitivo.
- `spring.jpa.hibernate.ddl-auto=update` apenas para desenvolvimento local inicial.
- Testes unitarios ja foram iniciados no modulo de Alunos.
- Flyway, Swagger, testes de integracao e autenticacao serao avaliados em fases futuras.
