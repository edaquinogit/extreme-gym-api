# Project Scope

## Objetivo do sistema

O Extreme Gym API tem como objetivo fornecer uma API REST para apoiar a gestao operacional de uma academia, organizando dados de alunos, planos, matriculas, pagamentos e check-ins.

O foco inicial e criar uma base tecnica clara, simples e profissional, adequada para evolucao incremental e apresentacao em portfolio.

## Escopo do MVP

O MVP deve contemplar, em ordem:

- Cadastro e gerenciamento de alunos.
- Cadastro e gerenciamento de planos.
- Criacao de matriculas.
- Registro de pagamentos.
- Registro de check-ins.
- Validacoes de entrada.
- Tratamento padronizado de erros.
- Testes automatizados para regras principais.

## Status por modulo

| Modulo | Status | Observacao |
| --- | --- | --- |
| Alunos | Implementado e refinado | CRUD basico, validacoes, tratamento de erros e testes unitarios |
| Planos | Implementado | CRUD basico, validacoes, nome unico, remocao logica e testes unitarios |
| Matriculas | Implementado | Conecta Alunos e Planos, impede matricula ativa duplicada, calcula vigencia e permite cancelamento |
| Pagamentos | Implementado | Registra pagamentos confirmados, impede duplicidade de pagamento PAGO e permite cancelamento logico |
| Check-ins | Implementado | Registra tentativas permitidas ou bloqueadas com motivo claro |
| Validacao de Acesso | Planejado | Fase futura apos Check-ins |

## Fora do escopo atual

- Autenticacao e autorizacao.
- Controle de perfis de usuario.
- Integracao com meios de pagamento.
- Envio de e-mails ou notificacoes.
- Relatorios avancados.
- Dashboard administrativo.
- Deploy em nuvem.
- Multiacademia ou multiempresa.
- Frontend.
- Swagger.
- Flyway.
- Dockerfile de aplicacao.
- Integracao com catraca.
- QR Code para acesso fisico.
- Face ID ou biometria facial.
- Controle fisico de acesso.
- Automacao de lembretes e retencao de alunos.
- Integracao com WhatsApp Business Platform ou provedores de mensageria.
- Entidades, services, controllers ou jobs para notificacoes.

Integracoes com catraca, QR Code, Face ID e controle fisico de acesso sao evolucoes futuras e nao fazem parte do escopo implementado ate o momento.

## Automacao de lembretes e retencao de alunos

Esta funcionalidade e uma evolucao futura e nao esta implementada no MVP atual.

No futuro, o sistema podera identificar alunos com matricula ativa e varios dias sem check-in, permitindo acoes de retencao e relacionamento. Exemplos de regras futuras:

- Aluno ha 7 dias sem frequentar a academia recebe lembrete de retorno.
- Aluno ha 10 dias sem frequentar e com mensalidade atrasada recebe lembrete financeiro ou relacional.

A primeira implementacao dessa evolucao podera ser apenas uma simulacao ou um registro interno de notificacoes, sem envio externo real.

O envio real por WhatsApp ficara fora do MVP atual e, quando avaliado, devera usar WhatsApp Business Platform ou provedor autorizado. Mensagens automaticas deverao respeitar opt-in do aluno, templates aprovados, custos da plataforma e cuidados com LGPD.

A regra de frequencia dependera do modulo de Check-ins. A regra de inadimplencia dependera da evolucao do modulo de Pagamentos com controle de vencimentos.

## Entidades planejadas

- `Aluno`
- `Plano`
- `Matricula`
- `Pagamento`
- `CheckIn`

Neste momento, `Aluno`, `Plano`, `Matricula`, `Pagamento` e `CheckIn` foram implementadas.

## Regras de negocio iniciais

- Um aluno deve possuir dados minimos para identificacao e contato.
- Email de aluno nao pode ser duplicado.
- Um plano deve possuir nome, valor e descricao basica.
- Uma matricula deve relacionar um aluno a um plano.
- Uma matricula deve usar apenas planos ativos.
- Um aluno nao pode possuir mais de uma matricula ativa ao mesmo tempo.
- Uma matricula cancelada nao deve ser excluida fisicamente.
- Um pagamento deve estar associado a uma matricula.
- Uma matricula deve estar ativa para receber pagamento.
- Um pagamento registrado inicia com status `PAGO`.
- Um pagamento cancelado nao deve ser excluido fisicamente.
- Um check-in deve estar associado a um aluno matriculado.
- Um check-in deve registrar tentativa permitida ou bloqueada.
- Aluno bloqueado, cancelado ou inadimplente nao pode realizar check-in permitido.
- Check-in permitido exige matricula ativa, nao vencida e com pagamento pago.
- Validacoes devem ser feitas na entrada da API usando Bean Validation.
- Regras devem ficar na camada de service, evitando logica de negocio em controllers.
- Controllers devem receber requisicoes, delegar ao service e retornar respostas HTTP adequadas.

## Ordem de implementacao

1. Setup local com PostgreSQL e Docker Compose. Concluido.
2. Estrutura inicial de pacotes. Concluido.
3. CRUD de alunos. Concluido.
4. Refinamento de Alunos com validacoes, erros padronizados e testes. Concluido.
5. CRUD de planos. Concluido.
6. Matriculas. Concluido.
7. Pagamentos. Concluido.
8. Check-ins. Concluido.
9. Validacao de Acesso.
10. Testes adicionais conforme crescimento do dominio.
11. Swagger.
12. Autenticacao JWT.
13. Preparacao para deploy futuro.
