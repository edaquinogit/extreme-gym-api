# Roadmap

## Fase 1: Setup e infraestrutura

- Confirmar Maven Wrapper funcionando.
- Configurar PostgreSQL com Docker Compose.
- Configurar `application.properties`.
- Documentar setup local e escopo inicial.

## Fase 2: CRUD de alunos

- Criar entidade de aluno.
- Criar DTOs de entrada e saida.
- Criar repository, service e controller.
- Adicionar validacoes basicas.

## Fase 3: CRUD de planos

- Criar entidade de plano.
- Criar DTOs de entrada e saida.
- Criar repository, service e controller.
- Validar dados principais do plano.

## Fase 4: Matriculas

- Relacionar alunos e planos.
- Criar entidade de matricula.
- Definir status inicial de matricula.
- Criar endpoints necessarios para cadastro e consulta.

## Fase 5: Pagamentos

- Criar entidade de pagamento.
- Associar pagamentos a matriculas.
- Definir status basicos de pagamento.
- Criar endpoints de registro e consulta.

## Fase 6: Check-in

- Criar entidade de check-in.
- Registrar entrada de aluno.
- Associar check-in ao aluno matriculado.

## Fase 7: Testes

- Adicionar testes unitarios para services.
- Adicionar testes de integracao para controllers.
- Validar cenarios principais do MVP.

## Fase 8: Swagger

- Adicionar documentacao interativa da API.
- Documentar endpoints, entradas, saidas e erros principais.

## Fase 9: Autenticacao JWT

- Adicionar Spring Security.
- Criar fluxo de autenticacao.
- Proteger endpoints conforme necessidade.

## Fase 10: Deploy futuro

- Preparar variaveis de ambiente.
- Revisar configuracoes para producao.
- Avaliar uso de migrations com Flyway.
- Planejar deploy em ambiente cloud.
