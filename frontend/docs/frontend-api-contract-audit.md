# Frontend x API Contract Audit

Sprint 0 - consolidacao tecnica do Extreme Gym Web.

## Escopo auditado

- Services: `authService`, `alunoService`, `planoService`, `matriculaService`, `pagamentoService`, `checkinService`, `acessoService`.
- Types internos: `Aluno`, `Plano`, `Matricula`, `Pagamento`, `Checkin`, `AcessoResponse`, `AuthUser`.
- Hooks que consomem API: `useResourceList`, `useDashboardData`, `useAuth` via `AuthContext`.
- Paginas dependentes de API: `LoginPage`, `DashboardPage`, `AlunosPage`, `PlanosPage`, `MatriculasPage`, `PagamentosPage`, `CheckinsPage`, `AcessoPage`, `CatracaPage`.
- Componentes com dados de dominio: `DataTable`, `StatusBadge`, `StatusDropdown`, `MetricCard`, `StateMessage`.

## Diagnostico inicial

O frontend ja possui uma base consistente com React, Vite, TypeScript strict, HTTP centralizado e autenticacao JWT. O risco principal encontrado foi a UI consumir alguns formatos que nao batem exatamente com os DTOs do backend. A Sprint 0 criou mappers explicitos em `src/mappers` para isolar a interface do formato bruto da API.

## Contratos por entidade

### Aluno

Backend confirmado:

- `GET /alunos`
- `GET /alunos/{id}`
- `POST /alunos`
- `PUT /alunos/{id}`
- `PATCH /alunos/{id}/status`
- `DELETE /alunos/{id}`

DTO de resposta: `id`, `nome`, `email`, `telefone`, `status`, `dataCadastro`.

Divergencia corrigida:

- Alteracao de status usava `PUT /alunos/{id}` com objeto completo. Foi alterada para `PATCH /alunos/{id}/status` com payload `{ status }`.

Pendencia:

- `DELETE /alunos/{id}` e tratado na UI como inativacao. O backend executa a regra de remocao/inativacao no service; manter a nomenclatura visual como "inativar" e a tecnica como `DELETE`.

### Plano

Backend confirmado:

- `GET /planos`
- `GET /planos/{id}`
- `POST /planos`
- `PUT /planos/{id}`
- `DELETE /planos/{id}`

DTO de resposta: `id`, `nome`, `descricao`, `valorMensal`, `duracaoEmDias`, `ativo`, `dataCadastro`.

Divergencias corrigidas por mapper:

- API `valorMensal` -> UI `valor`.
- API `duracaoEmDias` -> UI `duracaoDias`.
- API `ativo` -> UI `status` (`ATIVO` ou `INATIVO`) e `ativo`.

Pendencia:

- A tela ainda nao implementa formulario de criacao/edicao/inativacao de plano; os endpoints estao modelados no service para uso futuro.

### Matricula

Backend confirmado:

- `GET /matriculas`
- `GET /matriculas/{id}`
- `POST /matriculas`
- `PATCH /matriculas/{id}/cancelar`

DTO de resposta: `id`, `alunoId`, `alunoNome`, `planoId`, `planoNome`, `dataInicio`, `dataFim`, `status`, `dataCadastro`.

Divergencia corrigida por mapper:

- API `dataFim` -> UI `dataFim` e alias interno `dataVencimento`, porque telas existentes usam o nome de vencimento.

Pendencia:

- A tela ainda nao implementa criacao/cancelamento de matricula; endpoints confirmados ficam expostos no service.

### Pagamento

Backend confirmado:

- `GET /pagamentos`
- `GET /pagamentos/{id}`
- `GET /pagamentos/matricula/{matriculaId}`
- `POST /pagamentos`
- `PATCH /pagamentos/{id}/cancelar`

DTO de resposta: `id`, `matriculaId`, `alunoId`, `alunoNome`, `planoId`, `planoNome`, `valor`, `formaPagamento`, `status`, `dataPagamento`, `dataCadastro`.

Divergencias corrigidas:

- Frontend aceitava status `ATRASADO`, mas o backend possui `PAGO`, `PENDENTE` e `CANCELADO`.
- Frontend usava `dataVencimento`, mas o backend nao retorna vencimento em pagamento. O mapper preserva `dataVencimento` como `undefined`.

Pendencia:

- Dashboard mostra pagamentos vencidos como `0` enquanto nao houver campo/endpoint confirmado para vencimento financeiro.
- A tela ainda nao implementa registro/cancelamento visual de pagamento; endpoints confirmados ficam no service.

### Check-in

Backend confirmado:

- `GET /checkins`
- `GET /checkins/{id}`
- `GET /checkins/aluno/{alunoId}`
- `POST /checkins`

DTO de resposta: `id`, `alunoId`, `alunoNome`, `matriculaId`, `permitido`, `motivo`, `dataHora`.

Divergencias corrigidas por mapper:

- API `permitido` boolean -> UI `status` (`AUTORIZADO` ou `BLOQUEADO`) e `permitido`.
- API `motivo` -> UI `motivo` e `motivoBloqueio` quando o acesso nao foi permitido.

### Acesso / Validacao de acesso

Backend confirmado:

- `POST /acessos/validar`

DTO de request: `alunoId`.
DTO de response: `alunoId`, `alunoNome`, `acessoLiberado`, `motivo`, `matriculaId`, `dataValidadeMatricula`.

Divergencia:

- Nao havia divergencia critica, mas foi criado mapper para manter o padrao e proteger a UI contra nulos.

### Auth / Usuario

Backend confirmado:

- `POST /auth/login`
- `POST /auth/register`

Login request: `username`, `password`, com aliases aceitos pelo backend.
Register request: `nome`, `email`, `senha`.
Login response: `token`, `type`, `expiresInSeconds`, `usuarioId`, `nome`, `username`, `email`, `role`.

Divergencia tratada:

- Frontend mantem compatibilidade com respostas antigas (`accessToken`, `jwt`, `user`, `usuario`) dentro de `authMapper`, mas o formato confirmado do backend e `token`.

## Tratamento de erro

O projeto ja possui `HttpError` e tratamento centralizado em `httpClient`. A Sprint 0 manteve esse padrao:

- 401 limpa sessao e dispara `auth:unauthorized`.
- Mensagens `message` ou `error` retornadas pela API sao preservadas.
- Paginas seguem usando `useApiError` e `StateMessage`.

## Testes

Nao havia estrutura de testes configurada no frontend e nao existe script `npm run test` no `package.json`. Para a proxima etapa tecnica, recomenda-se instalar e configurar:

- `vitest`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `jsdom`

Testes prioritarios:

- `planoMapper`: `valorMensal`, `duracaoEmDias`, `ativo`.
- `checkinMapper`: `permitido` para `status`.
- `matriculaMapper`: `dataFim` para `dataVencimento`.
- `pagamentoMapper`: status validos e ausencia de `dataVencimento`.

## Pendencias para Sprint 1

- Implementar formularios de CRUD para planos, matriculas e pagamentos usando os services ja mapeados.
- Definir com backend se havera endpoint agregado para dashboard.
- Definir se pagamentos vencidos serao calculados por matricula/data final ou por um futuro campo financeiro.
- Adicionar testes unitarios dos mappers antes de expandir fluxos visuais.
