# Security Audit Actions

## Concluido nesta fase

- **Migrações e Schema**:
  - Flyway adicionado como mecanismo de migrations em PostgreSQL.
  - Migration inicial criada para o schema atual.
  - Índices únicos parciais adicionados para proteger regras críticas contra concorrência:
    - `uk_matriculas_aluno_ativa`: uma matrícula `ATIVA` por aluno;
    - `uk_pagamentos_matricula_pago`: um pagamento `PAGO` por matrícula.
- **Exclusão Lógica de Aluno (Soft Delete)**:
  - Alterada a ação de `DELETE /alunos/{id}` para marcar o aluno como `StatusAluno.INATIVO` em vez de exclusão física, protegendo o histórico financeiro e relatórios.
  - Uso de `@SQLDelete` e `@Where` no `Aluno.java` para interceptar as buscas e exclusões no Hibernate de forma transparente.
  - Nova migration Flyway criada para padronizar o valor default de `status` como `'ATIVO'`.
- **Swagger / OpenAPI**:
  - Acesso público a rotas do Swagger bloqueado por padrão em ambientes compartilhados/produção, restringindo a liberação irrestrita apenas para os profiles `dev` e `local` via Spring Security.
- **Segurança de Login e Credenciais**:
  - Substituição de senhas administrativas estáticas nos profiles locais por um mecanismo de geração dinâmica no startup (`AdminUserInitializer`), gerando senhas aleatórias fortes no console caso nenhuma senha seja explicitada.
  - Redução de segurança do registro público de usuários (`app.auth.registration-enabled=false`) por default nas configurações locais.
  - Aumento da segurança de senhas no registro de novos usuários (`RegisterRequest.java`), exigindo um mínimo de 12 caracteres.
- **Paginação Global**:
  - Limite máximo global configurado (`spring.data.web.pageable.max-page-size=100`) nas propriedades do Spring Data Web para mitigar vulnerabilidades DoS por sobrecarga de memória.
- **Logs de Testes**:
  - Redução da verbosidade de logs HTTP e de segurança do Spring em ambientes compartilhados e testes para evitar vazamento acidental de tokens JWT truncados.

## Observacoes de teste

- O profile `test` continua usando H2 com `spring.jpa.hibernate.ddl-auto=create-drop` e `spring.flyway.enabled=false`.
- O repositório de alunos foi modificado para sobrescrever o método `deleteAll()` com uma query SQL nativa, garantindo que o banco de dados em memória seja totalmente limpo entre os testes de integração (ignorando os filtros de `@Where` que antes causavam vazamento de estados e falha por constraint de email único).
- As constraints parciais de concorrência são específicas de PostgreSQL e ficam garantidas pela migration versionada. A suíte cobre a presença desses índices na migration e mantém testes de serviço para tratamento amigável de violações de integridade.
