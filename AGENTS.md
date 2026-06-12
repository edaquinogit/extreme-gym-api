# Extreme Gym API - Repo Boundaries

Este repositorio e a fonte oficial do backend Spring Boot da Extreme Gym.

## Frontend

O frontend oficial nao deve ser executado ou alterado a partir da pasta `frontend/` deste repositorio.

Use sempre o repositorio separado:

```text
../extreme-gym-web
https://github.com/edaquinogit/extreme-gym-web.git
```

Para subir o frontend localmente:

```bash
cd ../extreme-gym-web
npm run dev
```

Para alterar telas, componentes, estilos, rotas ou services do frontend, edite somente `../extreme-gym-web`.

## Backend

Use este repositorio para alteracoes da API, regras de negocio, DTOs, entidades, controllers, migrations, seguranca e documentacao do backend.

Para subir a API localmente:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
