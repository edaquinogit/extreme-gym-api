-- data_cadastro is a legacy column not mapped by the JPA entity (which uses criado_em from
-- AuditableEntity). Adding a DB-level default so inserts from the application never fail.
alter table usuarios  alter column data_cadastro set default current_timestamp;
alter table alunos    alter column data_cadastro set default current_timestamp;
