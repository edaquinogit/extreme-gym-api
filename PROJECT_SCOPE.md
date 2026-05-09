# Project Scope

## Objetivo do sistema

O Extreme Gym API tem como objetivo fornecer uma API REST para apoiar a gestao operacional de uma academia, organizando dados de alunos, planos, matriculas, pagamentos e check-ins.

O foco inicial e criar uma base tecnica clara, simples e profissional, adequada para evolucao incremental e apresentacao em portfolio.

## Escopo do MVP

O MVP deve contemplar, em ordem:

- Cadastro de alunos
- Cadastro de planos
- Criacao de matriculas
- Registro de pagamentos
- Registro de check-ins
- Validacoes basicas de entrada
- Tratamento padronizado de erros

## Fora do escopo por enquanto

- Autenticacao e autorizacao
- Controle de perfis de usuario
- Integracao com meios de pagamento
- Envio de e-mails ou notificacoes
- Relatorios avancados
- Dashboard administrativo
- Deploy em nuvem
- Multiacademia ou multiempresa
- Frontend

## Entidades planejadas

- `Aluno`
- `Plano`
- `Matricula`
- `Pagamento`
- `CheckIn`

Essas entidades ainda nao devem ser implementadas nesta etapa. Elas servem apenas como direcao de modelagem para as proximas fases.

## Regras de negocio iniciais

- Um aluno deve possuir dados minimos para identificacao e contato.
- Um plano deve possuir nome, valor e descricao basica.
- Uma matricula deve relacionar um aluno a um plano.
- Um pagamento deve estar associado a uma matricula.
- Um check-in deve estar associado a um aluno matriculado.
- Validacoes devem ser feitas na entrada da API usando Bean Validation.
- Regras devem ficar na camada de service, evitando logica de negocio em controllers.

## Ordem de implementacao

1. Finalizar setup local com PostgreSQL e Docker Compose.
2. Criar estrutura inicial de pacotes.
3. Implementar CRUD de alunos.
4. Implementar CRUD de planos.
5. Implementar matriculas.
6. Implementar pagamentos.
7. Implementar check-ins.
8. Adicionar testes automatizados.
9. Adicionar Swagger.
10. Adicionar autenticacao JWT.
11. Preparar deploy futuro.
