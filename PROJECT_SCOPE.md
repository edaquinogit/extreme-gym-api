# Project Scope

## Objetivo do sistema

O Extreme Gym API tem como objetivo fornecer uma API REST para apoiar a gestao operacional de uma academia, organizando dados de alunos, planos, matriculas, pagamentos e check-ins.

O foco inicial e criar uma base tecnica clara, simples e profissional, adequada para evolucao incremental e apresentacao em portfolio.

## Escopo do MVP

O MVP deve contemplar, em ordem:

- Cadastro e gerenciamento de alunos.
- Cadastro e gerenciamento de planos.
- Criacao de matriculas.
- Registro de pagamentos.
- Registro de check-ins.
- Validacoes de entrada.
- Tratamento padronizado de erros.
- Testes automatizados para regras principais.

## Status por modulo

| Modulo | Status | Observacao |
| --- | --- | --- |
| Alunos | Implementado e refinado | CRUD basico, validacoes, tratamento de erros e testes unitarios |
| Planos | Proximo modulo | Ainda nao implementado |
| Matriculas | Planejado | Depende de Alunos e Planos |
| Pagamentos | Planejado | Depende de Matriculas |
| Check-ins | Planejado | Depende da base de alunos e matriculas |

## Fora do escopo atual

- Autenticacao e autorizacao.
- Controle de perfis de usuario.
- Integracao com meios de pagamento.
- Envio de e-mails ou notificacoes.
- Relatorios avancados.
- Dashboard administrativo.
- Deploy em nuvem.
- Multiacademia ou multiempresa.
- Frontend.
- Swagger.
- Flyway.
- Dockerfile de aplicacao.
- Integracao com catraca.
- QR Code para acesso fisico.
- Face ID ou biometria facial.
- Controle fisico de acesso.

Integracoes com catraca, QR Code, Face ID e controle fisico de acesso sao evolucoes futuras e nao fazem parte do escopo implementado ate o momento.

## Entidades planejadas

- `Aluno`
- `Plano`
- `Matricula`
- `Pagamento`
- `CheckIn`

Neste momento, apenas `Aluno` foi implementada.

## Regras de negocio iniciais

- Um aluno deve possuir dados minimos para identificacao e contato.
- Email de aluno nao pode ser duplicado.
- Um plano deve possuir nome, valor e descricao basica.
- Uma matricula deve relacionar um aluno a um plano.
- Um pagamento deve estar associado a uma matricula.
- Um check-in deve estar associado a um aluno matriculado.
- Validacoes devem ser feitas na entrada da API usando Bean Validation.
- Regras devem ficar na camada de service, evitando logica de negocio em controllers.
- Controllers devem receber requisicoes, delegar ao service e retornar respostas HTTP adequadas.

## Ordem de implementacao

1. Setup local com PostgreSQL e Docker Compose. Concluido.
2. Estrutura inicial de pacotes. Concluido.
3. CRUD de alunos. Concluido.
4. Refinamento de Alunos com validacoes, erros padronizados e testes. Concluido.
5. CRUD de planos. Proximo passo.
6. Matriculas.
7. Pagamentos.
8. Check-ins.
9. Testes adicionais conforme crescimento do dominio.
10. Swagger.
11. Autenticacao JWT.
12. Preparacao para deploy futuro.
