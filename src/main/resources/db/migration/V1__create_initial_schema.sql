create table usuarios (
    id bigserial primary key,
    nome varchar(255) not null,
    email varchar(255) not null unique,
    username varchar(255) unique,
    password_hash varchar(255) not null,
    role varchar(255) not null,
    ativo boolean not null,
    data_cadastro timestamp not null
);

create table alunos (
    id bigserial primary key,
    nome varchar(255) not null,
    email varchar(255) not null unique,
    telefone varchar(255) not null,
    status varchar(255) not null,
    data_cadastro timestamp not null
);

create table planos (
    id bigserial primary key,
    nome varchar(255) not null unique,
    descricao varchar(255),
    valor_mensal numeric(10, 2) not null,
    duracao_em_dias integer not null,
    ativo boolean not null,
    data_cadastro timestamp not null
);

create table matriculas (
    id bigserial primary key,
    aluno_id bigint not null,
    plano_id bigint not null,
    data_inicio date not null,
    data_fim date not null,
    status varchar(255) not null,
    data_cadastro timestamp not null,
    constraint fk_matriculas_alunos foreign key (aluno_id) references alunos (id),
    constraint fk_matriculas_planos foreign key (plano_id) references planos (id)
);

create table pagamentos (
    id bigserial primary key,
    matricula_id bigint not null,
    valor numeric(10, 2) not null,
    forma_pagamento varchar(255) not null,
    status varchar(255) not null,
    data_pagamento timestamp not null,
    data_cadastro timestamp not null,
    constraint fk_pagamentos_matriculas foreign key (matricula_id) references matriculas (id)
);

create table check_ins (
    id bigserial primary key,
    aluno_id bigint not null,
    matricula_id bigint,
    data_hora timestamp not null,
    permitido boolean not null,
    motivo varchar(255) not null,
    constraint fk_check_ins_alunos foreign key (aluno_id) references alunos (id),
    constraint fk_check_ins_matriculas foreign key (matricula_id) references matriculas (id)
);

create unique index uk_matriculas_aluno_ativa
    on matriculas (aluno_id)
    where status = 'ATIVA';

create unique index uk_pagamentos_matricula_pago
    on pagamentos (matricula_id)
    where status = 'PAGO';
