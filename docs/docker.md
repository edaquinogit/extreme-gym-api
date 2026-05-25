# Docker e Ambientes

Este repositorio empacota somente a Extreme Gym API. O frontend oficial fica em um repositorio separado:

```text
https://github.com/edaquinogit/extreme-gym-web.git
```

Nao copie codigo React/Vite para este backend. Se uma pasta `frontend/` aparecer por acidente, ela deve ser ignorada/removida e nao deve entrar em commit da API.

## Arquivos

- `Dockerfile`: imagem multi-stage da API com Java 21.
- `docker-compose.yml`: stack padrao de desenvolvimento.
- `docker-compose.dev.yml`: stack explicita de desenvolvimento.
- `docker-compose.local.yml`: API isolada com H2, util para desenvolvimento rapido.
- `docker-compose.prod.example.yml`: exemplo de producao, sem expor PostgreSQL no host.
- `.env.example`: modelo de variaveis, sem segredos reais.

## Variaveis

Copie `.env.example` para `.env` e ajuste os valores:

```bash
cp .env.example .env
```

Principais variaveis:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `ADMIN_EMAIL`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `CORS_ALLOWED_ORIGINS`
- `SPRING_PROFILES_ACTIVE`
- `SKIP_TESTS`

Gere `JWT_SECRET` com:

```bash
openssl rand -base64 32
```

## Desenvolvimento

Subir API e PostgreSQL:

```bash
docker compose up -d --build
```

Ou usando o arquivo explicito:

```bash
docker compose -f docker-compose.dev.yml up -d --build
```

No ambiente de desenvolvimento, o PostgreSQL expoe `5432:5432` para facilitar acesso por ferramentas locais. A API usa a network interna para acessar `postgres:5432`.

Swagger fica habilitado no profile `dev`:

```text
http://localhost:8080/swagger-ui/index.html
```

## Local com H2

Para subir apenas a API com H2:

```bash
docker compose -f docker-compose.local.yml up -d --build
```

Esse modo usa `SPRING_PROFILES_ACTIVE=local`.

## Producao

Use `docker-compose.prod.example.yml` como base, nao como arquivo final com segredos reais.

```bash
docker compose -f docker-compose.prod.example.yml config
```

No exemplo de producao:

- `SPRING_PROFILES_ACTIVE=prod`;
- PostgreSQL nao expoe porta no host;
- API e PostgreSQL comunicam pela network interna;
- Swagger/OpenAPI ficam desabilitados pelo profile `prod`;
- `CORS_ALLOWED_ORIGINS` deve ser definido com o dominio real do frontend;
- secrets devem vir do orquestrador, secret manager ou pipeline seguro.

## Build da Imagem

Build rapido local, sem testes dentro da imagem:

```bash
docker build -t extreme-gym-api:local .
```

Build de release/teste, executando a suite Maven:

```bash
docker build --build-arg SKIP_TESTS=false -t extreme-gym-api:test .
```

O `Dockerfile` usa `SKIP_TESTS=true` por padrao para nao quebrar o fluxo local, mas o build de release deve usar `SKIP_TESTS=false`.

## Healthcheck

Nao foi adicionada dependencia nova de Spring Actuator. Os healthchecks usam o endpoint simples existente:

```text
GET /
```

Resposta esperada:

```json
{"message":"Extreme Gym API is running"}
```
