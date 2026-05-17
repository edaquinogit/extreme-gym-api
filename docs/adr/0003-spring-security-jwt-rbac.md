# Spring Security JWT RBAC

## Status

Aceito

## Contexto

O MVP atual ainda nao possui autenticacao nem autorizacao. Endpoints administrativos, financeiros e operacionais precisam ser protegidos antes de uso real ou integracao com frontend.

O sistema possui perfis claros de acesso: `ADMIN`, `RECEPCAO`, `PROFESSOR`, `ALUNO` e `CATRACA`.

## Decisao

Implementar seguranca com Spring Security, JWT e controle de acesso baseado em roles.

O modulo de usuarios deve incluir entidade de usuario, perfil, repository, service, controller de autenticacao, DTOs de login e resposta, provider de token JWT e `UserDetailsService` customizado.

Rotas de autenticacao devem ser publicas. Rotas administrativas devem exigir token e role adequada.

## Consequencias

- Endpoints sensiveis deixam de ficar publicos.
- O backend fica preparado para frontend em React + TypeScript.
- Regras de permissao precisam ser testadas com cuidado.
- Swagger deve continuar acessivel em desenvolvimento com configuracao explicita.
