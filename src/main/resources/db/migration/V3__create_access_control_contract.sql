create table dispositivos_acesso (
    id bigserial primary key,
    nome varchar(255) not null,
    tipo varchar(255) not null,
    status varchar(255) not null,
    modo_operacao varchar(255) not null,
    identificador_externo varchar(255) unique,
    api_key_hash varchar(255) not null,
    ultima_comunicacao_em timestamp,
    fabricante varchar(255),
    modelo varchar(255),
    ip_local varchar(255),
    unidade varchar(255),
    criado_em timestamp not null,
    atualizado_em timestamp not null
);

create table credenciais_acesso (
    id bigserial primary key,
    aluno_id bigint not null,
    tipo varchar(255) not null,
    identificador_externo varchar(255) not null,
    fornecedor varchar(255),
    status varchar(255) not null,
    cadastrado_em timestamp not null,
    revogado_em timestamp,
    termo_aceito_em timestamp,
    versao_termo varchar(255),
    criado_em timestamp not null,
    atualizado_em timestamp not null,
    constraint fk_credenciais_acesso_alunos foreign key (aluno_id) references alunos (id),
    constraint uk_credenciais_acesso_tipo_identificador unique (tipo, identificador_externo)
);

create table eventos_acesso (
    id bigserial primary key,
    aluno_id bigint,
    dispositivo_id bigint not null,
    matricula_id bigint,
    origem varchar(255) not null,
    modo varchar(255) not null,
    resultado varchar(255) not null,
    motivo varchar(255) not null,
    data_hora_evento timestamp not null,
    data_hora_recebimento timestamp not null,
    sincronizado boolean not null,
    identificador_externo_evento varchar(255),
    idempotency_key varchar(255) not null unique,
    credencial_tipo varchar(255),
    identificador_externo varchar(255),
    criado_em timestamp not null,
    constraint fk_eventos_acesso_alunos foreign key (aluno_id) references alunos (id),
    constraint fk_eventos_acesso_dispositivos foreign key (dispositivo_id) references dispositivos_acesso (id),
    constraint fk_eventos_acesso_matriculas foreign key (matricula_id) references matriculas (id)
);

create index idx_dispositivos_acesso_status on dispositivos_acesso (status);
create index idx_credenciais_acesso_aluno on credenciais_acesso (aluno_id);
create index idx_credenciais_acesso_status on credenciais_acesso (status);
create index idx_eventos_acesso_aluno on eventos_acesso (aluno_id);
create index idx_eventos_acesso_dispositivo on eventos_acesso (dispositivo_id);
create index idx_eventos_acesso_data_hora_evento on eventos_acesso (data_hora_evento);
