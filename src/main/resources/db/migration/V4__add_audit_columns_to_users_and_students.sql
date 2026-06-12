alter table usuarios
    add column if not exists criado_por varchar(255),
    add column if not exists criado_em timestamp,
    add column if not exists atualizado_por varchar(255),
    add column if not exists atualizado_em timestamp;

alter table alunos
    add column if not exists criado_por varchar(255),
    add column if not exists criado_em timestamp,
    add column if not exists atualizado_por varchar(255),
    add column if not exists atualizado_em timestamp;

update usuarios
set criado_em = coalesce(criado_em, data_cadastro, current_timestamp)
where criado_em is null;

update usuarios
set atualizado_em = coalesce(atualizado_em, criado_em)
where atualizado_em is null;

update alunos
set criado_em = coalesce(criado_em, data_cadastro, current_timestamp)
where criado_em is null;

update alunos
set atualizado_em = coalesce(atualizado_em, criado_em)
where atualizado_em is null;
