# Risco: JWT em localStorage

## Estado Atual

O frontend armazena token e usuario no `localStorage`. O backend usa Bearer token stateless com expiracao configurada.

## Risco

Se uma vulnerabilidade XSS permitir executar JavaScript no navegador da recepcao/admin, o atacante pode ler o token no `localStorage` e reutilizar a sessao ate a expiracao.

Impactos:

- acesso indevido ao painel;
- alteracao de dados conforme perfil roubado;
- vazamento de dados pessoais;
- risco maior se conta compartilhada ou admin ficar logado.

## Mitigacoes Atuais

- HTTPS obrigatorio em ambiente real.
- Token com expiracao curta.
- RBAC no backend.
- Registro publico desabilitado por padrao.
- Secrets obrigatorios por ambiente.
- Treinamento para logout e contas individuais.

## Por Que Nao Bloqueia Staging/Piloto

Staging e piloto controlado podem seguir desde que:

- nao haja dados reais amplos sem necessidade;
- operadores usem contas individuais;
- navegador seja confiavel;
- nao exista HTML dinamico inseguro;
- dependencias sejam validadas;
- suporte tecnico acompanhe.

## Evolucao Recomendada

Antes de producao ampla ou quando houver mais operadores:

- migrar para cookie `HttpOnly`, `Secure`, `SameSite`;
- ou access token em memoria com refresh token seguro;
- adicionar CSP e security headers;
- implementar rotacao/revogacao de sessao se necessario;
- reduzir privilegios dos perfis de recepcao.
