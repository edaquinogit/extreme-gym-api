# Spring Security JWT RBAC

## Status

Aceito

## Contexto

No inicio do MVP, os endpoints administrativos, financeiros e operacionais ainda nao possuíam autenticacao nem autorizacao. Eles precisavam ser protegidos antes de uso real ou integracao com frontend.

O sistema possui perfis claros de acesso: `ADMIN`, `RECEPCAO`, `PROFESSOR`, `ALUNO` e `CATRACA`.

## Decisao

Implementar seguranca com Spring Security, JWT e controle de acesso baseado em roles.

O modulo de usuarios deve incluir entidade de usuario, perfil, repository, service, controller de autenticacao, DTOs de login e resposta, provider de token JWT e `UserDetailsService` customizado.

Rotas de autenticacao devem ser publicas. Rotas administrativas devem exigir token e role adequada.

## Atualizacao

A primeira implementacao de seguranca foi concluida:

- `POST /auth/login` autentica usuario e retorna JWT.
- `POST /auth/register` existe, mas depende de `AUTH_REGISTRATION_ENABLED`.
- Registro publico nao aceita role externa e cria apenas usuario `RECEPCAO`.
- `ADMIN`, `CATRACA` e roles privilegiadas nao podem ser criadas pelo endpoint publico.
- O filtro JWT reconsulta o usuario no banco a cada request autenticada.
- Usuario inexistente, inativo ou invalido nao autentica.
- A role atual do banco prevalece sobre a role antiga do token.
- O desligamento global de seguranca ficou restrito ao profile `test`.

## Consequencias

- Endpoints sensiveis deixam de ficar publicos.
- O backend fica preparado para frontend em React + TypeScript.
- Regras de permissao precisam ser testadas com cuidado.
- Swagger deve continuar acessivel em desenvolvimento com configuracao explicita.
