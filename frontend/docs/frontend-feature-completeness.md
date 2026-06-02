# Frontend Feature Completeness

Sprint 1 - testes minimos e CRUD funcional.

## Diagnostico inicial

- A Sprint 0 deixou contratos API/UI documentados e mappers dedicados em `src/mappers`.
- As paginas de Planos, Matriculas e Pagamentos ainda tinham tabelas iniciais e botoes sem acao real.
- Os services ja expunham os endpoints necessarios para esta sprint:
  - `planoService`: listar, obterPorId, criar, atualizar, inativar.
  - `matriculaService`: listar, obterPorId, criar, cancelar.
  - `pagamentoService`: listar, obterPorId, listarPorMatricula, registrar, cancelar.
- O projeto ja tinha componentes reutilizaveis suficientes para manter o visual existente: `Modal`, `ConfirmDialog`, `FormField`, `StateMessage`, `StatusBadge` e `DataTable`.

## Dependencias instaladas

- `vitest`: runner de testes compativel com Vite.
- `@testing-library/react`: base para testes de componentes React quando necessario.
- `@testing-library/jest-dom`: matchers DOM para testes React.
- `jsdom`: ambiente DOM para Vitest.
- `react-hook-form`: controle de formularios sem biblioteca visual.
- `zod`: validacao tipada dos formularios.

Nao foram instaladas bibliotecas pesadas, AG Grid, Cypress, Playwright ou bibliotecas de UI.

## Testes adicionados

Foram criados testes unitarios para todos os mappers criticos da Sprint 0:

- `src/mappers/planoMapper.test.ts`
- `src/mappers/alunoMapper.test.ts`
- `src/mappers/matriculaMapper.test.ts`
- `src/mappers/pagamentoMapper.test.ts`
- `src/mappers/checkinMapper.test.ts`
- `src/mappers/acessoMapper.test.ts`
- `src/mappers/authMapper.test.ts`

Os testes cobrem conversao API/UI, payloads enviados ao backend, campos opcionais, status suportados e ausencia de dependencia de campos inexistentes.

## Formularios implementados

### Planos

- Criacao de plano via `POST /planos`.
- Edicao de plano via `PUT /planos/{id}`.
- Inativacao de plano via `DELETE /planos/{id}`.
- Validacoes: nome minimo de 3 caracteres, valor maior que zero e duracao em dias maior que zero.
- Payload preserva `valorMensal` e `duracaoEmDias` via `planoMapper`.

### Matriculas

- Criacao de matricula via `POST /matriculas`.
- Cancelamento de matricula via `PATCH /matriculas/{id}/cancelar`.
- Selects reais de aluno e plano usando `alunoService.listar` e `planoService.listar`.
- Validacoes: aluno obrigatorio, plano obrigatorio e data de inicio obrigatoria.
- Listagem exibe `dataFim`, sem depender de `dataVencimento`.

### Pagamentos

- Registro de pagamento via `POST /pagamentos`.
- Cancelamento de pagamento via `PATCH /pagamentos/{id}/cancelar`.
- Filtro real por matricula via `GET /pagamentos/matricula/{matriculaId}`.
- Validacoes: matricula obrigatoria, valor maior que zero e forma de pagamento obrigatoria.
- O formulario nao envia `status`, `dataPagamento` ou `dataVencimento`, pois esses campos nao existem no request confirmado.

## Botoes corrigidos

- Novo plano.
- Editar plano.
- Inativar plano.
- Nova matricula.
- Cancelar matricula.
- Registrar pagamento.
- Cancelar pagamento.

## Estados e feedback

- As paginas mantem loading, empty state e error state.
- Acoes de criacao, edicao, cancelamento e inativacao exibem sucesso ou erro amigavel.
- As listas fazem `reload` apos mutacoes.
- Valores seguem formatacao BRL com `formatCurrency`.
- Datas seguem formatacao pt-BR com `formatDate`.
- Status usam `StatusBadge`.

## Autorizacao por perfil

- Planos: acoes restritas a `ADMIN`.
- Matriculas e Pagamentos: acoes restritas a `ADMIN` e `RECEPCAO`.
- `CATRACA` nao gerencia planos, matriculas ou pagamentos.
- Se o usuario autenticado vier sem `role` ou `roles`, as acoes sensiveis ficam indisponiveis por seguranca.

## Contratos preservados

- Plano: `valorMensal` da API continua virando `valor` na UI.
- Plano: `duracaoEmDias` da API continua virando `duracaoDias` na UI.
- Plano: `ativo` continua virando status `ATIVO`/`INATIVO`.
- Matricula: `dataFim` e o campo exibido na listagem.
- Pagamento: status suportados continuam limitados a `PAGO`, `PENDENTE` e `CANCELADO`.
- Pagamento: a UI nao usa `ATRASADO`.
- Pagamento: a UI nao depende de `dataVencimento`.
- Check-in/Acesso: `permitido`/`motivo` continuam protegidos pelos mappers.

## Limitacoes conhecidas

- O backend confirmado nao expõe `status` ou `dataPagamento` no payload de registro de pagamento; por isso esses campos nao foram enviados pelo formulario.
- O backend confirmado nao expõe campo `ativo` no payload de criacao/edicao de plano; status e tratado pela acao de inativacao.
- Nao foi criada cobertura de testes total nem testes E2E nesta sprint.

## Proximos passos recomendados

- Adicionar testes de componentes para os formularios mais criticos.
- Confirmar com o backend se pagamento deve aceitar registro direto como `PAGO` ou se o status sempre nasce pela regra do servidor.
- Confirmar uma politica oficial para perfis sem `role` no token/resposta.
- Evoluir filtros de listagem sem alterar contratos ja protegidos.
