# JWT Security Configuration Guide

> **Status:** Este guia descreve a configuração segura de JWT após auditoria de segurança.  
> **Última atualização:** Maio 2026  
> **Responsável:** Security Team

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Configuração por Ambiente](#configuração-por-ambiente)
- [Geração de Secret Seguro](#geração-de-secret-seguro)
- [Variáveis de Ambiente Requeridas](#variáveis-de-ambiente-requeridas)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## 🔒 Visão Geral

### O Problema Corrigido

**Antes (Vulnerável):**
```properties
jwt.secret=${JWT_SECRET:dev-secret-change-me}  # ❌ Fallback inseguro!
```

**Depois (Seguro):**
```properties
jwt.secret=${JWT_SECRET}  # ✅ Sem fallback, REQUERIDO
```

### Mudanças Implementadas

1. **Remoção de Fallbacks Inseguros**
   - `application.properties`: Sem defaults para `jwt.secret` e credenciais admin
   - Falhará no startup se variáveis não estiverem configuradas (fail-fast)

2. **Validação de Segurança no Startup** (`JwtSecurityValidator`)
   - Verifica se secret tem mínimo 32 caracteres
   - Detecta padrões inseguros (dev-secret, test-secret, etc)
   - Lança exceção clara se configuração é insegura

3. **Profiles Específicos por Ambiente**
   - `application-dev.properties`: Secret gerado para dev local
   - `application-local.properties`: Secret gerado para testes locais
   - `application-prod.properties`: REQUERIDO via variável de ambiente

---

## 🌍 Configuração por Ambiente

### Development (profile: `dev`)

**Arquivo:** `application-dev.properties`

```properties
jwt.secret=${JWT_SECRET:dev-MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==}
jwt.expiration-minutes=1440

app.admin.email=${ADMIN_EMAIL:admin@extremegym.local}
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD:admin@123Dev!}
```

**Uso Local:**
```bash
# Em desenvolvimento local, use o fallback configurado
export SPRING_PROFILES_ACTIVE=dev
java -jar extreme-gym-api.jar

# OU sobreescreva via variável de ambiente
export JWT_SECRET="seu-secret-customizado"
java -jar extreme-gym-api.jar
```

### Local Testing (profile: `local`)

**Arquivo:** `application-local.properties`

```properties
jwt.secret=${JWT_SECRET:local-MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==}
jwt.expiration-minutes=1440

app.admin.email=${ADMIN_EMAIL:admin@extremegym.local}
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD:admin@123Local!}
```

### Production (profile: `prod`)

**Arquivo:** `application-prod.properties`

```properties
jwt.secret=${JWT_SECRET}  # ❌ REQUERIDO, sem fallback
jwt.expiration-minutes=60

app.admin.email=${ADMIN_EMAIL}  # ❌ REQUERIDO, sem fallback
app.admin.username=${ADMIN_USERNAME}  # ❌ REQUERIDO, sem fallback
app.admin.password=${ADMIN_PASSWORD}  # ❌ REQUERIDO, sem fallback
```

**Implantação em Produção:**
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="seu-secret-gerado-com-openssl"
export ADMIN_EMAIL="admin@empresa.com"
export ADMIN_USERNAME="admin_seguro_2026"
export ADMIN_PASSWORD="PasswordComplexo123!@#"
export DATABASE_URL="jdbc:postgresql://prod-db:5432/gym"
export DATABASE_USERNAME="prod_user"
export DATABASE_PASSWORD="password_complexo_aqui"

java -jar extreme-gym-api.jar
```

---

## 🔐 Geração de Secret Seguro

### Requisitos

- ✅ Mínimo **32 caracteres**
- ✅ Contém variação de caracteres (Base64 é ideal)
- ✅ Gerado de forma **criptograficamente aleatória**
- ❌ Nunca reutilize entre ambientes
- ❌ Nunca commite no Git

### Método 1: OpenSSL (Recomendado)

```bash
# Gera um secret Base64 com 32+ caracteres
openssl rand -base64 32

# Exemplo de output:
# MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==
```

### Método 2: OpenSSL (Alternativa)

```bash
# Gera um secret hexadecimal com 64 caracteres (32 bytes)
openssl rand -hex 32

# Exemplo de output:
# a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6
```

### Método 3: Python

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Exemplo de output:
# 7Y_VQ6j1vJ8z2K9x_0L1m2N3o4P5q6R
```

---

## 📋 Variáveis de Ambiente Requeridas

### ✅ Produção (OBRIGATÓRIAS)

| Variável | Descrição | Exemplo | Segurança |
|----------|-----------|---------|-----------|
| `JWT_SECRET` | Secret para assinatura HMAC-SHA256 | `MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==` | 🔴 CRÍTICA |
| `ADMIN_EMAIL` | Email da conta admin | `admin@empresa.com` | 🟡 IMPORTANTE |
| `ADMIN_USERNAME` | Username da conta admin | `admin_prod_2026` | 🟡 IMPORTANTE |
| `ADMIN_PASSWORD` | Senha da conta admin | `Pass@123!Complexo` | 🔴 CRÍTICA |
| `DATABASE_URL` | URL de conexão PostgreSQL | `jdbc:postgresql://host:5432/db` | 🔴 CRÍTICA |
| `DATABASE_USERNAME` | Usuário do banco de dados | `app_user` | 🔴 CRÍTICA |
| `DATABASE_PASSWORD` | Senha do banco de dados | `DbPassword123!@#` | 🔴 CRÍTICA |

### 📌 Desenvolvimento (OPCIONAIS com Defaults)

```bash
# Defaults em application-dev.properties
export SPRING_PROFILES_ACTIVE=dev

# Opcional: sobreescrever defaults
export JWT_SECRET="seu-secret-customizado"
export ADMIN_PASSWORD="sua-senha-customizada"
```

---

## 🆘 Troubleshooting

### Erro: "JWT_SECRET não configurado"

```
╔════════════════════════════════════════════════════════════╗
║ 🔴 ERRO DE CONFIGURAÇÃO JWT - Falha na Inicialização      ║
╠════════════════════════════════════════════════════════════╣
║ JWT_SECRET não configurado
║ Exporte a variável de ambiente JWT_SECRET com um valor seguro.
║ Sugestão:
║ export JWT_SECRET="$(openssl rand -base64 32)"
╚════════════════════════════════════════════════════════════╝
```

**Solução:**
```bash
export JWT_SECRET="$(openssl rand -base64 32)"
java -jar extreme-gym-api.jar
```

### Erro: "JWT Secret detectado como inseguro"

```
║ JWT Secret contém valores padrão como 'dev-secret' ou 'test-secret'.
```

**Causa:** Você exportou um valor de desenvolvimen como produção.

**Solução:**
```bash
# Gere um secret novo e seguro
NEW_SECRET=$(openssl rand -base64 32)
echo "Use este secret em produção: $NEW_SECRET"
export JWT_SECRET="$NEW_SECRET"
```

### Erro: "JWT Secret muito curto"

```
║ JWT Secret muito curto (16 chars, esperado: 32)
```

**Causa:** O secret tem menos de 32 caracteres.

**Solução:**
```bash
# Use este comando para gerar secret válido
openssl rand -base64 32
```

---

## 💡 Best Practices

### 1. **Nunca Commite Secrets**

```bash
# ❌ NUNCA faça isto
echo 'JWT_SECRET=xyz123' >> .env
git add .env
git commit -m "Add secrets"

# ✅ SEMPRE use variáveis de ambiente
echo '.env' >> .gitignore
export JWT_SECRET="$(openssl rand -base64 32)"
```

### 2. **Rotação de Secrets**

Implemente rotação periódica (recomendado: **a cada 90 dias** em produção):

```bash
# 1. Gere novo secret
NEW_SECRET=$(openssl rand -base64 32)

# 2. Atualize em seu sistema de secrets (AWS Secrets Manager, Azure Key Vault, etc)
# 3. Reinicie a aplicação
# 4. Registre em seu audit log

echo "Secret rotacionado em: $(date)" >> audit.log
```

### 3. **Diferentes Secrets por Ambiente**

✅ **CORRETO:**
```bash
# Dev
export JWT_SECRET_DEV="dev-MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng=="

# Staging
export JWT_SECRET_STAGING="stg-OTkwNzA4MzIxMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU="

# Produção
export JWT_SECRET_PROD="prd-$(openssl rand -base64 32)"
```

❌ **INCORRETO:**
```bash
# Usar mesmo secret em todos os ambientes
export JWT_SECRET="same-secret-everywhere"
```

### 4. **Monitorar Tentativas de Falsificação**

Adicione logs quando validação falhar:

```java
// Em JwtAuthenticationFilter
if (jwtService.validateToken(token).isEmpty()) {
    logger.warn("JWT validation failed for token from IP: {}", 
                request.getRemoteAddr());
    // Considere alertar/bloquear após N tentativas
}
```

### 5. **Documentar Credenciais Admin**

Depois de primeira implantação, **altere credenciais admin padrão**:

```bash
# 1. Login com credenciais padrão
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin@123Dev!"}'

# 2. Altere email/username/password via API ou banco de dados
# 3. Teste com novas credenciais
# 4. Documente em sistema seguro (Vault, etc)
```

---

## 📚 Referências

- [RFC 7518 - JSON Web Algorithms (JWA)](https://tools.ietf.org/html/rfc7518)
- [OWASP - JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security - JWT](https://spring.io/blog/2015/01/12/the-api-gateway-pattern-part-1)

---

## 📞 Suporte

**Perguntas sobre JWT Security?**

1. Verifique este documento
2. Consulte o arquivo [SECURITY_AUDIT.md](../SECURITY_AUDIT.md)
3. Abra uma issue no repositório
4. Contate o Security Team
