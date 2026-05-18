# Ações de Auditoria de Segurança Pendentes

Este documento registra os pontos identificados na auditoria de segurança que não foram implementados na fase inicial de hardening e devem ser tratados em ciclos futuros de desenvolvimento.

## 1. Banco de Dados e Migrations
- **Implementação de Flyway/Liquibase**: Atualmente o projeto depende de `ddl-auto`. É crítico migrar para uma ferramenta de versionamento de banco de dados para garantir consistência entre ambientes.
- **Constraints Únicas Parciais**: Avaliar a necessidade de constraints mais complexas no banco de dados para evitar duplicidade de dados sensíveis.

## 2. API e Listagens
- **Paginação Obrigatória**: Nem todas as listagens possuem paginação. Implementar `Pageable` em todos os endpoints de busca para evitar ataques de negação de serviço (DoS) por consumo excessivo de memória.
- **Soft Delete**: Implementar exclusão lógica (soft delete) para entidades críticas como Aluno e Matrícula, visando manter histórico auditável.

## 3. Regras de Negócio e Segurança
- **Revogação de Tokens (Token Versioning)**: Implementar um mecanismo de versão de token ou blacklist para permitir a invalidação imediata de tokens JWT em caso de comprometimento ou logout.
- **Regras de Renovação de Matrícula**: Automatizar e proteger a lógica de expiração e renovação de matrículas para evitar acessos indevidos.
- **Modelagem Financeira**: Refinar a segurança nos fluxos de pagamento, garantindo que descontos e pagamentos parciais sejam devidamente auditados e protegidos contra manipulação.

## 4. Infraestrutura
- **Secrets Management**: Embora o `JWT_SECRET` tenha sido externalizado, recomenda-se o uso de um cofre de senhas (como HashiCorp Vault ou AWS Secrets Manager) em vez de apenas variáveis de ambiente em produção.
- **Rate Limiting**: Implementar limitação de taxa nos endpoints de autenticação para mitigar ataques de força bruta.

---
*Documento gerado em 18 de Maio de 2026 como parte do processo de hardening de segurança.*
