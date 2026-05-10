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

Status: concluida.

- Entidade `CheckIn` criada.
- DTOs de entrada e saida criados.
- Repository, service e controller criados.
- Registro de tentativa de check-in implementado.
- Check-in associado ao aluno e, quando permitido, a matricula ativa.
- Bloqueio de aluno `BLOQUEADO`, `CANCELADO` ou `INADIMPLENTE`.
- Bloqueio quando nao existe matricula `ATIVA`.
- Bloqueio quando a matricula esta vencida pela data final.
- Bloqueio quando nao existe pagamento `PAGO` para a matricula.
- Tentativas bloqueadas tambem sao registradas com motivo claro.
- Testes unitarios do service adicionados.

## Fase 7: Validacao de Acesso

Status: concluida.

- DTOs de entrada e saida criados.
- Controller `POST /acessos/validar` criado.
- Service de validacao de acesso criado como camada somente leitura.
- Decisao de acesso separada da acao de registrar check-in.
- Bloqueio de aluno `BLOQUEADO`, `CANCELADO` ou `INADIMPLENTE`.
- Exigencia de matricula `ATIVA`, nao vencida e com pagamento `PAGO`.
- Resposta com motivo claro para acesso liberado ou bloqueado.
- Testes unitarios do service adicionados.
- Integracoes fisicas como catraca, QR Code e Face ID mantidas fora desta fase inicial.

## Fase 8: Testes adicionais e qualidade

Status: planejada conforme evolucao do dominio.

- Expandir testes unitarios para novos services.
- Avaliar testes de integracao para controllers.
- Validar cenarios principais do MVP.
- Revisar cobertura de regras de negocio e contratos HTTP.

## Fase 9: Swagger

Status: planejada para fase futura.

- Adicionar documentacao interativa da API.
- Documentar endpoints, entradas, saidas e erros principais.

## Fase 10: Autenticacao JWT

Status: planejada para fase futura.

- Adicionar Spring Security.
- Criar fluxo de autenticacao.
- Proteger endpoints conforme necessidade.

## Fase 11: Deploy futuro

Status: planejada para fase futura.

- Preparar variaveis de ambiente.
- Revisar configuracoes para producao.
- Avaliar uso de migrations com Flyway.
- Planejar deploy em ambiente cloud.

## Evolucao futura: Automacao de lembretes e retencao de alunos

Status: visao futura, fora do MVP atual.

- Identificar alunos com matricula ativa e varios dias sem check-in.
- Avaliar regra de lembrete de retorno para aluno ha 7 dias sem frequentar.
- Avaliar regra de lembrete financeiro ou relacional para aluno ha 10 dias sem frequentar e com mensalidade atrasada.
- Iniciar a evolucao, se necessario, com simulacao ou registro interno de notificacoes.
- Manter envio real por WhatsApp fora do MVP atual.
- Usar WhatsApp Business Platform ou provedor autorizado somente em uma etapa futura de integracao real.
- Respeitar opt-in do aluno, templates aprovados, custos da plataforma e cuidados com LGPD.
- Depender do modulo de Check-ins para calcular frequencia.
- Depender da evolucao do modulo de Pagamentos com controle de vencimentos para calcular inadimplencia.
- Nao criar entidades, services, controllers, jobs ou integracoes de notificacao nesta fase.
