# Flyway Para Migrations

## Status

Aceito

## Contexto

O projeto usa JPA/Hibernate para persistencia. No inicio do MVP, o profile de desenvolvimento utilizava `spring.jpa.hibernate.ddl-auto=update`, enquanto producao usava `validate`.

Essa abordagem facilita o MVP, mas nao oferece versionamento confiavel do schema, dificulta reproducibilidade e aumenta o risco de mudancas acidentais no banco.

## Decisao

Adotar Flyway como ferramenta oficial de versionamento de banco de dados.

As migrations devem ficar em `src/main/resources/db/migration` e seguir nomenclatura como:

- `V1__create_initial_schema.sql`
- `V2__create_users_and_roles.sql`
- `V3__add_constraints_and_indexes.sql`

A troca para `ddl-auto=validate` deve ser gradual e validada contra o banco local atual.

## Atualizacao

A primeira rodada foi implementada:

- Flyway foi adicionado ao projeto.
- A migration inicial `V1__create_initial_schema.sql` foi criada em `src/main/resources/db/migration`.
- Os profiles `dev` e `prod` usam `ddl-auto=validate` com Flyway ativo.
- O profile `test` continua com H2, `ddl-auto=create-drop` e Flyway desabilitado para manter a suite independente de PostgreSQL real.
- Constraints criticas de PostgreSQL foram versionadas na migration inicial.

## Consequencias

- Mudancas de schema passam a ser rastreaveis.
- Ambientes ficam mais consistentes.
- Constraints e indices podem ser versionados com seguranca.
- A primeira migration precisa considerar o estado atual do banco para evitar quebra em desenvolvimento.
