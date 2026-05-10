# Roadmap

## Fase 1: Setup e infraestrutura

Status: concluida.

- Maven Wrapper funcionando.
- PostgreSQL configurado com Docker Compose.
- `application.properties` configurado para ambiente local.
- Profile de teste com H2.
- Documentacao inicial de setup e escopo.

## Fase 2: CRUD de alunos

Status: concluida e refinada.

- Entidade `Aluno` criada.
- Enum `StatusAluno` criado.
- DTOs de entrada e saida criados.
- Repository, service e controller criados.
- CRUD basico implementado.
- Validacoes aplicadas em `AlunoRequestDTO`.
- Tratamento de erros padronizado.
- Regra de email duplicado implementada no cadastro e na atualizacao.
- Testes unitarios do service passando.
- Endpoints testados manualmente em `localhost:8080`.
- Endpoint raiz `GET /` criado para indicar que a API esta rodando.

### Criterios de conclusao do modulo de Alunos

- CRUD basico implementado.
- Validacoes aplicadas para nome, email e telefone.
- Tratamento de erros padronizado com `timestamp`, `status`, `error`, `message` e `path`.
- Erros de validacao retornando campos invalidos.
- Testes unitarios relevantes passando.
- Endpoints testados manualmente:
  - `GET /`
  - `GET /alunos`
  - `POST /alunos`
  - `PUT /alunos/{id}`
  - `DELETE /alunos/{id}`

## Fase 3: CRUD de planos

Status: concluida.

- Entidade `Plano` criada.
- DTOs de entrada e saida criados.
- Repository, service e controller criados.
- Validacoes aplicadas em `PlanoRequestDTO`.
- Regra de nome duplicado implementada no cadastro e na atualizacao.
- Remocao logica implementada, desativando o plano.
- Testes unitarios do service adicionados.
- Mesmo padrao de erros e organizacao usado em Alunos mantido.

## Fase 4: Matriculas

Status: concluida.

- Entidade `Matricula` criada.
- Enum `StatusMatricula` criado.
- DTOs de entrada e saida criados.
- Repository, service e controller criados.
- Relacionamento com `Aluno` e `Plano` implementado.
- Status inicial `ATIVA` definido.
- Data final calculada pela duracao do plano.
- Bloqueio de plano inativo em matriculas.
- Bloqueio de mais de uma matricula `ATIVA` para o mesmo aluno.
- Cancelamento por alteracao de status para `CANCELADA`.
- Testes unitarios do service adicionados.

## Fase 5: Pagamentos

Status: concluida.

- Entidade `Pagamento` criada.
- Enums `StatusPagamento` e `FormaPagamento` criados.
- DTOs de entrada e saida criados.
- Repository, service e controller criados.
- Relacionamento com `Matricula` implementado.
- Registro de pagamentos confirmados com status inicial `PAGO`.
- Data de pagamento e data de cadastro definidas automaticamente.
- Bloqueio de pagamento para matricula `CANCELADA`.
- Bloqueio de pagamento para matricula `VENCIDA` nesta versao inicial.
- Bloqueio de pagamento `PAGO` duplicado para a mesma matricula.
- Cancelamento por alteracao de status para `CANCELADO`.
- Testes unitarios do service adicionados.

## Fase 6: Check-ins

Status: planejada.

- Criar entidade de check-in.
- Registrar entrada de aluno.
- Associar check-in ao aluno matriculado.

## Fase 7: Testes adicionais

Status: planejada conforme evolucao do dominio.

- Expandir testes unitarios para novos services.
- Avaliar testes de integracao para controllers.
- Validar cenarios principais do MVP.

## Fase 8: Swagger

Status: planejada para fase futura.

- Adicionar documentacao interativa da API.
- Documentar endpoints, entradas, saidas e erros principais.

## Fase 9: Autenticacao JWT

Status: planejada para fase futura.

- Adicionar Spring Security.
- Criar fluxo de autenticacao.
- Proteger endpoints conforme necessidade.

## Fase 10: Deploy futuro

Status: planejada para fase futura.

- Preparar variaveis de ambiente.
- Revisar configuracoes para producao.
- Avaliar uso de migrations com Flyway.
- Planejar deploy em ambiente cloud.
