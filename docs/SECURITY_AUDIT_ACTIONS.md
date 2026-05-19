# Security Audit Actions

## Concluido nesta fase

- Flyway adicionado como mecanismo de migrations em PostgreSQL.
- Migration inicial criada para o schema atual.
- Indices unicos parciais adicionados para proteger regras criticas contra concorrencia:
  - `uk_matriculas_aluno_ativa`: uma matricula `ATIVA` por aluno;
  - `uk_pagamentos_matricula_pago`: um pagamento `PAGO` por matricula.
- Listagens principais passaram a aceitar `page`, `size` e `sort`, mantendo resposta em array para compatibilidade inicial.
- Violacoes de integridade nas regras criticas sao convertidas em erro de negocio amigavel nos services.

## Pendente por risco de alteracao ampla

- Exclusao fisica de aluno: o modelo possui `StatusAluno`, mas mudar `DELETE /alunos/{id}` para soft delete exigiria revisar queries, listagens, filtros, regras de matricula/check-in/pagamento e contrato com consumidores. Proxima fase recomendada:
  - bloquear exclusao quando houver historico relacionado; ou
  - transformar exclusao em alteracao para `StatusAluno.CANCELADO`, documentando a mudanca de contrato.

## Observacoes de teste

- O profile `test` continua usando H2 com `spring.jpa.hibernate.ddl-auto=create-drop` e `spring.flyway.enabled=false`.
- As constraints parciais sao especificas de PostgreSQL e ficam garantidas pela migration versionada. A suite cobre a presenca desses indices na migration e mantem testes de servico para tratamento amigavel de violacoes de integridade.
