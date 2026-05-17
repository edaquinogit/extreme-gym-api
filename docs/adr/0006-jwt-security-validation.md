# ADR-006: Validação Segura de JWT Secret no Startup

**Data:** Maio 17, 2026  
**Status:** Aceito  
**Contexto:** Auditoria de Segurança - Vulnerabilidade de JWT Secret Hardcoded  
**Decisor:** Security Team

---

## Problem Statement

### Vulnerabilidade Identificada

A aplicacao utilizava fallback inseguro para JWT Secret:

```properties
jwt.secret=${JWT_SECRET:dev-secret-change-me}
```

Se a variavel de ambiente `JWT_SECRET` nao fosse exportada, a aplicacao usaria o valor padrao `dev-secret-change-me`, permitindo:

1. **Falsificação de Tokens**: Qualquer pessoa conhecendo este secret poderia gerar tokens validos
2. **Comprometimento de Autenticação**: Tokens falsificados poderiam acessar qualquer recurso
3. **Risco em Produção**: Se deploy em prod sem exportar JWT_SECRET, secret padrão seria usado

### Impacto

- **Severidade**: 🔴 CRÍTICA
- **Tipo**: Segurança - Autenticação JWT
- **Vetores de Ataque**: Falsificação de tokens, escalação de privilégios
- **Princípios Violados**: Fail-secure, Defense-in-depth

---

## Decision

Implementar **validação robusta de JWT Secret no startup** com os seguintes princípios:

1. **Fail-Fast**: Aplicacao falha se configuração é insegura
2. **Explicit is Better Than Implicit**: Sem defaults inseguros
3. **Environment-Aware**: Diferentes requerimentos por profile
4. **Secure-by-Default**: Rejeita secrets fracos

### Solução Implementada

#### 1. Remover Fallbacks Inseguros

**application.properties** (Principal - Sem Secret)
```properties
jwt.secret=${JWT_SECRET}  # Sem :valor_padrao inseguro
```

#### 2. Fornecerfall Seguro Por Profile

**application-dev.properties** (Desenvolvimento)
```properties
jwt.secret=${JWT_SECRET:dev-MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==}
```

**application-prod.properties** (Produção)
```properties
jwt.secret=${JWT_SECRET}  # Sem fallback - REQUERIDO
```

#### 3. Validação Declarativa no Startup

Criar `JwtSecurityValidator` que:

```java
// 1. Verifica se secret é null/vazio
if (secret == null || secret.isBlank()) {
    throwConfigurationException("JWT_SECRET não configurado");
}

// 2. Detecta padrões inseguros
if (containsInsecurePattern(secret)) {  // dev-secret, test-secret, etc
    throwConfigurationException("JWT Secret detectado como inseguro");
}

// 3. Valida comprimento mínimo
if (secret.length() < MINIMUM_SECRET_LENGTH) {  // 32 caracteres
    throwConfigurationException("JWT Secret muito curto");
}

// 4. Lança exceção clara se inválido
throwConfigurationException(
    "JWT Secret não seguro",
    "Gere com: openssl rand -base64 32"
);
```

#### 4. Testes de Segurança

Adicionar testes que validam:

```java
// Deve falhar sem JWT_SECRET
shouldFailWhenJwtSecretIsNotConfigured()

// Deve falhar com padrão inseguro
shouldFailWhenJwtSecretContainsInsecurePattern()

// Deve falhar com secret curto
shouldFailWhenJwtSecretIsTooShort()

// Deve aceitar secret válido
shouldAcceptValidJwtSecret()
```

#### 5. Documentação Clara

Criar:
- `JWT_SECURITY_CONFIGURATION.md` - Guia completo
- `JWT_QUICK_REFERENCE.md` - Quick start
- `SECURITY_STATUS.md` - Status de correções

---

## Rationale

### Por que Fail-Fast?

```
❌ Alternativa Ruim: Usar secret padrão silenciosamente
- Aplicação inicia "normalmente"
- Tokens falsificados são aceitos
- Vulnerabilidade descoberta tarde em produção
- Difícil de debugar

✅ Escolha Correta: Falhar no startup
- Erro claro e imediato
- Força configuração explícita
- Impossível usar acidentalmente em produção
- Detecta problema nos primeiros segundos
```

### Por que 32 Caracteres Mínimo?

- HMAC-SHA256 recomenda mínimo 256 bits
- Base64 requer 32 caracteres para representar 256 bits
- Menos que isso permite ataque de força bruta em tempo aceitável

### Por que Diferentes Requerimentos por Profile?

```
Development/Local: Permitem defaults
- Facilita primeiro startup
- Desenvolvedor não precisa memorizar secrets
- Isolado - nunca vai para produção

Production: Obrigatório configurar
- Força responsabilidade do DevOps
- Impossível deploy acidental com secret inseguro
- Auditável - documentado qual secret foi usado
```

### Por que Detectar Padrões Inseguros?

```java
❌ Detectadas como inseguras:
- "dev-secret-change-me"
- "test-secret-12345"
- "secret" (muito genérico)
- "12345678" (muito simples)

✅ Aceitas como seguras:
- "MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng=="  (Base64 gerado)
- "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6"  (Hex gerado)
```

Isso previne que:
1. Devs copiem secret padrão para produção
2. Teste com secret fraco
3. Commit acidental de secret de exemplo

---

## Consequences

### Positivas ✅

1. **Segurança Melhorada**: Impossível usar secret inseguro
2. **Fail-Fast**: Detecta problema na inicialização
3. **Documentação**: Guia claro para configuração
4. **Testable**: Casos de falha testados automaticamente
5. **Produção-Ready**: Força boas práticas de DevOps
6. **Clear Code**: Validação explícita e legível

### Negativas ⚠️

1. **Breaking Change**: Dev precisa exportar JWT_SECRET em prod
2. **Mais Configuração**: Mais variáveis de ambiente
3. **Startup Mais Lento**: Uma validação extra (~5ms)

### Mitigação

- Documentação clara e exemplos prontos
- Script de geração automática de secrets
- Mensagens de erro explicativas
- Quick reference para desenvolvedores

---

## Alternatives Considered

### 1. Usar Library JWT Estabelecida (JJWT)

❌ Rejeitada porque:
- Projeto usa implementação customizada por escolha arquitetônica
- Migração seria maior escopo fora desta auditoria
- Validação atual é suficiente com melhorias

### 2. Usar Azure Key Vault / AWS Secrets Manager

❌ Rejeitada porque:
- Seria dependency externo
- Complexidade adicional
- Validação local é suficiente como primeira camada

### 3. Gerar Secret Automaticamente se Não Configurado

❌ Rejeitada porque:
- Secret gerado seria diferente a cada restart
- Tokens anteriores se tornariam inválidos
- Não é auditável (qual secret foi usado?)

### 4. Permitir Secret Padrão Apenas em Desenvolvimento

❌ Rejeitada porque:
- Difícil diferenciar "dev genuíno" vs "esqueceu de configurar em prod"
- Profiles existem justamente para isso

✅ Implementado: Diferentes requerimentos por profile

---

## Implementation

### Arquivos Criados

1. `JwtSecurityValidator.java` (90 linhas)
   - Validação declarativa
   - Falha clara com sugestões
   - Detecta padrões inseguros

2. `JwtSecurityValidatorTest.java` (100 linhas)
   - 8 testes de segurança
   - Cobre cenários de falha

### Arquivos Modificados

1. `application.properties`
   - Remover fallback inseguro

2. `application-dev.properties`
   - Adicionar secret dev seguro

3. `application-prod.properties`
   - Deixar claro: REQUERIDO

### Documentação

1. `JWT_SECURITY_CONFIGURATION.md`
   - Guia completo por environment
   - Scripts para gerar secrets
   - Troubleshooting

2. `JWT_QUICK_REFERENCE.md`
   - Quick start
   - Checklist de produção

---

## Validation

### Testes

```bash
# Executar testes de segurança
./mvnw test -Dtest=JwtSecurityValidatorTest
# Resultado esperado: 8/8 testes passando
```

### Cenários Validados

| Cenário | Antes | Depois | Status |
|---------|-------|--------|--------|
| Dev sem JWT_SECRET | ✅ Usa padrão inseguro | ✅ Usa default dev | ✓ SEGURO |
| Prod sem JWT_SECRET | ❌ Usa padrão inseguro | ❌ Falha no startup | ✓ SEGURO |
| Secret falsificado | ❌ Aceito | ❌ Rejeitado | ✓ SEGURO |
| Secret < 32 chars | ❌ Aceito | ❌ Falha no startup | ✓ SEGURO |

---

## Future Enhancements

1. **Rotação Periódica de Secrets**
   - Implementar rotação a cada 90 dias
   - Suportar múltiplos secrets (old + novo)

2. **Integração com Vault**
   - HashiCorp Vault para secrets management
   - Automatic secret injection

3. **Auditoria de Segurança**
   - Log quando validação falha
   - Alertas para múltiplas tentativas

4. **Migração para JJWT Library**
   - Considerar no futuro
   - Mantém compatibilidade com tokens atuais

5. **OAuth2 / OpenID Connect**
   - Evolução futura
   - Suporte para múltiplos provedores

---

## References

- [RFC 7518 - JSON Web Algorithms (JWA)](https://tools.ietf.org/html/rfc7518)
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [CWE-334: Use of Insufficiently Random Values](https://cwe.mitre.org/data/definitions/334.html)
- [CWE-347: Improper Verification of Cryptographic Signature](https://cwe.mitre.org/data/definitions/347.html)

---

## Sign-off

| Role | Date | Status |
|------|------|--------|
| Security Review | 17/05/2026 | ✅ Approved |
| Architecture | Pending | ⏳ |
| Lead Developer | Pending | ⏳ |
| QA | Pending | ⏳ |

---

**ADR-006 Status: ACCEPTED**  
**Supersedes:** None  
**Superseded by:** None  
**Last Updated:** Maio 17, 2026
