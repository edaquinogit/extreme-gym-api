# 🔒 Quick Reference: JWT Security Setup

**Para desenvolvedores, DevOps e QA**

---

## 🚀 Quick Start

### Development (Rápido)

```bash
# Apenas ative o profile dev - tudo funciona com defaults
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

✅ **Usa:** Secret dev gerado + Credenciais dev  
✅ **Acesso:** http://localhost:8080  
✅ **Admin:** admin / admin@123Dev!

---

### Local (Com Banco H2)

```bash
# Use profile local - ideal para testes isolados
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

✅ **Banco:** H2 em memória  
✅ **Admin:** admin / admin@123Local!

---

### Production (Completo)

```bash
# 1. Gere um secret seguro
SECRET=$(openssl rand -base64 32)
echo "Seu secret: $SECRET"

# 2. Configure as variáveis
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="$SECRET"
export ADMIN_EMAIL="seu-admin@empresa.com"
export ADMIN_USERNAME="seu_username_seguro"
export ADMIN_PASSWORD="P@ssw0rd!Complexo123"
export DATABASE_URL="jdbc:postgresql://host:5432/gym"
export DATABASE_USERNAME="db_user"
export DATABASE_PASSWORD="db_pass_complexa"

# 3. Execute
java -jar extreme-gym-api.jar
```

❌ **Sem variáveis:** Falhará no startup (fail-fast)  
✅ **Com variáveis:** Inicializará normalmente

---

## ⚙️ Variáveis de Ambiente Requeridas

### Por Profile

**DEV** (Opcionais - tem defaults)
```bash
export JWT_SECRET="seu-custom-secret"  # Opcional
export ADMIN_PASSWORD="custom-pass"     # Opcional
```

**LOCAL** (Opcionais - tem defaults)
```bash
export JWT_SECRET="seu-custom-secret"  # Opcional
export ADMIN_PASSWORD="custom-pass"     # Opcional
```

**PROD** (Obrigatórios - SEM defaults)
```bash
export JWT_SECRET="seu-secret-seguro"              # ❌ OBRIGATÓRIO
export ADMIN_EMAIL="admin@empresa.com"             # ❌ OBRIGATÓRIO
export ADMIN_USERNAME="admin_2026"                 # ❌ OBRIGATÓRIO
export ADMIN_PASSWORD="P@ssw0rd!Complexo123"       # ❌ OBRIGATÓRIO
export DATABASE_URL="jdbc:postgresql://..."        # ❌ OBRIGATÓRIO
export DATABASE_USERNAME="db_user"                 # ❌ OBRIGATÓRIO
export DATABASE_PASSWORD="db_pass_complexa"        # ❌ OBRIGATÓRIO
```

---

## 🔐 Gerar Secret Seguro

### Opção 1: OpenSSL (Recomendado)

```bash
openssl rand -base64 32
# Output: MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==
```

### Opção 2: Python

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
# Output: 7Y_VQ6j1vJ8z2K9x_0L1m2N3o4P5q6R
```

### ✅ Copiar para Seu Secret Manager

```bash
# AWS Secrets Manager
aws secretsmanager create-secret --name extreme-gym-jwt-secret \
  --secret-string "MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng=="

# Azure Key Vault
az keyvault secret set --vault-name my-vault \
  --name extreme-gym-jwt-secret \
  --value "MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng=="
```

---

## ✅ Teste a Configuração

### 1. Verificar Startup

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar

# ✅ SUCESSO: Aplicação inicia normalmente
# ❌ ERRO: Exceção JwtSecurityConfigurationException → faltam variáveis
```

### 2. Validar Token JWT

```bash
# Fazer login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin@123Dev!"}' | jq -r '.token')

# Verificar token
echo $TOKEN

# Usar em requisição
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/alunos
```

### 3. Testar Rejeição de Token Falsificado

```bash
# Tentar com token fakeeee
FAKE_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -H "Authorization: Bearer $FAKE_TOKEN" \
  http://localhost:8080/alunos

# Resultado esperado: 401 Unauthorized
```

---

## 🆘 Problemas Comuns

### "JWT_SECRET não configurado"

```bash
# ❌ PROBLEMA
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar

# ✅ SOLUÇÃO
export JWT_SECRET="$(openssl rand -base64 32)"
java -jar app.jar
```

### "JWT Secret muito curto"

```bash
# ❌ PROBLEMA
export JWT_SECRET="short"  # < 32 caracteres

# ✅ SOLUÇÃO
export JWT_SECRET="MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng=="  # >= 32 chars
```

### "JWT Secret detectado como inseguro"

```bash
# ❌ PROBLEMA
export JWT_SECRET="dev-secret-change-me"
export JWT_SECRET="test-secret-123"

# ✅ SOLUÇÃO
export JWT_SECRET="$(openssl rand -base64 32)"
```

---

## 📖 Documentação Completa

Para detalhes completos, veja:

- **[JWT_SECURITY_CONFIGURATION.md](docs/JWT_SECURITY_CONFIGURATION.md)** - Guia completo
- **[SECURITY_STATUS.md](SECURITY_STATUS.md)** - Status de correções
- **[SECURITY_AUDIT.md](SECURITY_AUDIT.md)** - Auditoria completa

---

## 🎯 Checklist de Implantação

### Antes de Colocar em Prod

- [ ] Gerar JWT_SECRET com `openssl rand -base64 32`
- [ ] Gerar ADMIN_PASSWORD complexa (12+ chars)
- [ ] Configurar todas as variáveis no seu secret manager
- [ ] Testar em staging com variáveis de prod
- [ ] Executar testes: `./mvnw test`
- [ ] Verificar logs na startup (sem erros)
- [ ] Confirmar que consegue fazer login

### Deploy

```bash
# 1. Build
./mvnw clean package

# 2. Tag
git tag v1.0.0-security-fixed

# 3. Deploy com variáveis
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET="seu-secret-gerado" \
  -e ADMIN_EMAIL="admin@empresa.com" \
  -e ADMIN_USERNAME="admin_prod" \
  -e ADMIN_PASSWORD="P@ssw0rd!Complexo123" \
  -e DATABASE_URL="jdbc:postgresql://..." \
  -e DATABASE_USERNAME="db_user" \
  -e DATABASE_PASSWORD="db_pass" \
  -p 8080:8080 \
  extreme-gym-api:latest
```

---

## 📊 Arquivos Relacionados

| Arquivo | Propósito |
|---------|-----------|
| `application.properties` | Base (principal, sem secrets) |
| `application-dev.properties` | Desenvolvimento com defaults seguros |
| `application-local.properties` | Local com H2 e defaults seguros |
| `application-prod.properties` | Produção (REQUER variáveis) |
| `JwtSecurityValidator.java` | Validador de segurança no startup |
| `JwtSecurityValidatorTest.java` | Testes de segurança |

---

**Last Updated:** Maio 17, 2026  
**Versão:** 1.0 - Post Security Fix
