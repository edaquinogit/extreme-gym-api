# API Contract

Contrato atual da Extreme Gym API.

Base URL local:

```text
http://localhost:8080
```

## Padrao de erro

Erros de negocio e recursos nao encontrados seguem o formato:

```json
{
  "timestamp": "2026-05-09T22:23:21.85908786",
  "status": 404,
  "error": "Not Found",
  "message": "Aluno nao encontrado com id: 2",
  "path": "/alunos/2"
}
```

Erros de validacao incluem o objeto `errors`:

```json
{
  "timestamp": "2026-05-09T22:22:57.568945756",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados invalidos",
  "path": "/alunos",
  "errors": {
    "email": "Email deve ser valido",
    "nome": "Nome deve ter no minimo 3 caracteres",
    "telefone": "Telefone deve ter entre 8 e 15 caracteres"
  }
}
```

## GET /

Objetivo: verificar se a API esta rodando.

Metodo HTTP: `GET`

Path: `/`

Response `200 OK`:

```json
{
  "message": "Extreme Gym API is running"
}
```

Possiveis status HTTP:

- `200 OK`: API em execucao.

## POST /alunos

Objetivo: cadastrar um aluno.

Metodo HTTP: `POST`

Path: `/alunos`

Request:

```json
{
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "telefone": "71999990000"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "telefone": "71999990000",
  "status": "ATIVO",
  "dataCadastro": "2026-05-09T22:12:56.60334"
}
```

Possiveis status HTTP:

- `201 Created`: aluno cadastrado.
- `400 Bad Request`: dados invalidos.
- `400 Bad Request`: email ja cadastrado.

## GET /alunos

Objetivo: listar alunos cadastrados.

Metodo HTTP: `GET`

Path: `/alunos`

Response `200 OK`:

```json
[
  {
    "id": 1,
    "nome": "Ana Silva",
    "email": "ana.silva@email.com",
    "telefone": "71999990000",
    "status": "ATIVO",
    "dataCadastro": "2026-05-09T22:12:56.60334"
  }
]
```

Possiveis status HTTP:

- `200 OK`: lista retornada. Pode retornar lista vazia.

## GET /alunos/{id}

Objetivo: buscar um aluno pelo identificador.

Metodo HTTP: `GET`

Path: `/alunos/{id}`

Response `200 OK`:

```json
{
  "id": 1,
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "telefone": "71999990000",
  "status": "ATIVO",
  "dataCadastro": "2026-05-09T22:12:56.60334"
}
```

Possiveis status HTTP:

- `200 OK`: aluno encontrado.
- `404 Not Found`: aluno nao encontrado.

## PUT /alunos/{id}

Objetivo: atualizar os dados de um aluno.

Metodo HTTP: `PUT`

Path: `/alunos/{id}`

Request:

```json
{
  "nome": "Ana Oliveira",
  "email": "ana.silva@email.com",
  "telefone": "71988887777"
}
```

Response `200 OK`:

```json
{
  "id": 1,
  "nome": "Ana Oliveira",
  "email": "ana.silva@email.com",
  "telefone": "71988887777",
  "status": "ATIVO",
  "dataCadastro": "2026-05-09T22:12:56.60334"
}
```

Possiveis status HTTP:

- `200 OK`: aluno atualizado.
- `400 Bad Request`: dados invalidos.
- `400 Bad Request`: email pertence a outro aluno.
- `404 Not Found`: aluno nao encontrado.

## DELETE /alunos/{id}

Objetivo: remover um aluno pelo identificador.

Metodo HTTP: `DELETE`

Path: `/alunos/{id}`

Response `204 No Content`:

```text
Sem corpo de resposta.
```

Possiveis status HTTP:

- `204 No Content`: aluno removido.
- `404 Not Found`: aluno nao encontrado.
