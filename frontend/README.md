# Extreme Gym Web

Frontend administrativo da Extreme Gym, preparado para consumir a Extreme Gym API em Spring Boot.

## Stack

- React
- TypeScript
- Vite
- CSS global responsivo
- Fetch API com cliente HTTP centralizado

## Requisitos

- Node.js
- npm
- Extreme Gym API rodando localmente ou em uma URL acessivel

## Configuracao

Crie um arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Configure a URL base da API:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH_REGISTRATION_ENABLED=false
```

> O projeto tambem aceita `VITE_API_URL` por compatibilidade com a configuracao anterior.

## Rodando localmente

Instale as dependencias:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm run dev
```

Acesse `http://localhost:5173/`.

## Scripts

```bash
npm run dev
npm run build
npm run lint
npm run preview
```

## Rotas

- `/login`
- `/` e `/dashboard`
- `/alunos`
- `/planos`
- `/matriculas`
- `/pagamentos`
- `/checkins`

## Integracao com API

A camada HTTP fica em `src/services/httpClient.ts`.

Ela centraliza:

- URL base via variavel de ambiente
- envio do JWT em `Authorization: Bearer <token>`
- parse de resposta JSON/texto
- tratamento basico de erro 401 com limpeza da sessao

Services por dominio:

- `authService.login()` chama `POST /auth/login`
- `alunoService.listar()` chama `GET /alunos`
- `planoService.listar()` chama `GET /planos`
- `matriculaService.listar()` chama `GET /matriculas`
- `pagamentoService.listar()` chama `GET /pagamentos`
- `checkinService.listar()` chama `GET /checkins`

## Login

O login envia:

```json
{
  "username": "admin@extremegym.local",
  "password": "valor-definido-em-ADMIN_PASSWORD-ou-exibido-nos-logs-da-API"
}
```

A resposta esperada deve conter `token` e `type: "Bearer"`. O frontend tambem aceita `accessToken` ou `jwt` por compatibilidade.

Tambem existe a opcao `Criar usuario`, que chama `POST /auth/register` e autentica automaticamente quando o backend retornar o token. Esse fluxo fica oculto por padrao e deve ser habilitado apenas em ambiente de desenvolvimento com `VITE_AUTH_REGISTRATION_ENABLED=true` no frontend e `AUTH_REGISTRATION_ENABLED=true` na API. O backend cria usuarios de registro publico com perfil `RECEPCAO`.

Credenciais de desenvolvimento:

```text
email: admin@extremegym.local
username: admin
password: defina ADMIN_PASSWORD antes de subir a API ou use a senha temporaria exibida nos logs
role: ADMIN
```

Fluxo manual esperado:

1. Acesse `/login`.
2. Informe `admin@extremegym.local` e a senha configurada em `ADMIN_PASSWORD`.
3. Confirme o redirecionamento para `/dashboard`.
4. Recarregue a pagina e confira se a sessao permanece ativa.
5. Clique em `Sair` e confirme o retorno para `/login`.

## Status do projeto

Esta etapa prepara a arquitetura inicial, rotas privadas, layout administrativo, autenticacao JWT e telas estruturais de listagem. Formularios completos, CRUD, paginacao, filtros avancados, dashboard real e deploy ficam como proximos incrementos.

## Proximos passos

- Confirmar contrato final do `POST /auth/login`
- Conectar dashboard a endpoints agregados reais
- Implementar formularios de cadastro e edicao
- Adicionar paginacao e filtros server-side
- Avaliar adocao de React Router quando a navegacao crescer
- Adicionar testes automatizados para auth e services
