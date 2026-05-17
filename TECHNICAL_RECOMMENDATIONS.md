# 🔧 RECOMENDAÇÕES TÉCNICAS - Extreme Gym Security

## 1. CORRIGIR JWT SECRET & ADMIN CREDENTIALS

### Backend - application.properties

```properties
# ❌ ANTES (INSEGURO):
jwt.secret=${JWT_SECRET:dev-secret-change-me}
app.admin.email=${ADMIN_EMAIL:admin@extremegym.com}
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD:admin123}

# ✅ DEPOIS (SEGURO):
jwt.secret=${JWT_SECRET}
app.admin.email=${ADMIN_EMAIL}
app.admin.username=${ADMIN_USERNAME}
app.admin.password=${ADMIN_PASSWORD}
```

### Deploy com Variáveis de Ambiente

```bash
# Production deployment
export JWT_SECRET="seu-secret-256-bit-aleatorio"
export ADMIN_EMAIL="admin@empresa.com"
export ADMIN_USERNAME="admin_prod"
export ADMIN_PASSWORD="SenhaForte@2024!Aleatorio123"
export DATABASE_URL="jdbc:postgresql://prod-db:5432/extreme_db"
export DATABASE_USERNAME="produser"
export DATABASE_PASSWORD="SenhaForte@2024!Aleatorio456"

java -jar app.jar --spring.profiles.active=prod
```

### Gerar JWT Secret Seguro

```bash
# Linux/Mac
openssl rand -base64 64

# Exemplo output:
# a8b9c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4a3b2c1d0e9f8g7h6i5j4k3l2m1n0o
```

---

## 2. IMPLEMENTAR TOKENS COM REFRESH LOGIC

### Adicionar Dependência

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### Novo JwtService com Refresh Token

```java
package com.extreme.gym.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    
    private final SecretKey key;
    private final long accessTokenExpiration;  // 15 minutos
    private final long refreshTokenExpiration; // 7 dias
    
    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-expiration-minutes:15}") long accessExpiration,
        @Value("${jwt.refresh-expiration-days:7}") long refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessExpiration * 60 * 1000;
        this.refreshTokenExpiration = refreshExpiration * 24 * 60 * 60 * 1000;
    }
    
    // Access Token (curta duração)
    public String generateAccessToken(Usuario usuario) {
        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("uid", usuario.getId())
            .claim("role", usuario.getRole().name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Refresh Token (longa duração)
    public String generateRefreshToken(Usuario usuario) {
        return Jwts.builder()
            .subject(usuario.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Validar e extrair claims
    public Optional<Claims> validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            return Optional.of(claims);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            return Optional.empty();
        }
    }
}
```

### Nova LoginResponse com Tokens

```java
package com.extreme.gym.dto.auth;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    UsuarioResponse usuario
) {}
```

### Endpoint de Refresh Token

```java
@PostMapping("/auth/refresh")
public ResponseEntity<LoginResponse> refresh(
    @RequestHeader("Authorization") String authorization
) {
    String token = authorization.replace("Bearer ", "");
    return jwtService.validateToken(token)
        .map(claims -> {
            Usuario usuario = usuarioService.findByEmail(claims.getSubject());
            return ResponseEntity.ok(
                new LoginResponse(
                    jwtService.generateAccessToken(usuario),
                    jwtService.generateRefreshToken(usuario),
                    new UsuarioResponse(usuario)
                )
            );
        })
        .orElseGet(() -> ResponseEntity.status(401).build());
}
```

---

## 3. REMOVER FLAG DE SEGURANÇA

### SecurityConfig.java

```java
// ❌ ANTES:
@Bean
public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    @Value("${app.security.enabled:true}") boolean securityEnabled
) throws Exception {
    // ...
    if (!securityEnabled) {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
    // ...
}

// ✅ DEPOIS (sem flag):
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, exception) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado"))
            .accessDeniedHandler((request, response, exception) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado")))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/auth/**").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers("/usuarios/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/alunos/**").hasAnyRole("ADMIN", "RECEPCAO", "PROFESSOR")
            .requestMatchers("/alunos/**").hasAnyRole("ADMIN", "RECEPCAO")
            .requestMatchers("/planos/**").hasAnyRole("ADMIN", "RECEPCAO")
            .requestMatchers("/matriculas/**").hasAnyRole("ADMIN", "RECEPCAO")
            .requestMatchers("/pagamentos/**").hasAnyRole("ADMIN", "RECEPCAO")
            .requestMatchers(HttpMethod.GET, "/checkins/**").hasAnyRole("ADMIN", "RECEPCAO", "PROFESSOR")
            .requestMatchers("/checkins/**").hasAnyRole("ADMIN", "RECEPCAO", "CATRACA")
            .requestMatchers("/acessos/**").hasAnyRole("ADMIN", "RECEPCAO", "CATRACA")
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

## 4. IMPLEMENTAR RATE LIMITING

### Adicionar Dependência

```xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.10.0</version>
</dependency>
```

### RateLimitInterceptor

```java
package com.extreme.gym.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }
    
    private Bucket createNewBucket() {
        // 5 tentativas a cada 15 minutos
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        
        // Aplicar rate limit apenas em /auth/login
        if (!request.getRequestURI().equals("/auth/login")) {
            return true;
        }
        
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            return true;
        }
        
        response.setStatus(429); // Too Many Requests
        response.getWriter().write("{\"error\": \"Muitas tentativas. Tente novamente em 15 minutos.\"}");
        return false;
    }
    
    private String getClientKey(HttpServletRequest request) {
        // Usar IP do cliente
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}

// Registrar no WebMvcConfigurer
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor());
    }
}
```

---

## 5. IMPLEMENTAR AUDITORIA DE LOGIN

### Entity AuditLog

```java
package com.extreme.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String usuario; // Email
    private String acao;    // LOGIN, LOGOUT, ACESSO_NEGADO, CREATE_ALUNO, etc
    private LocalDateTime timestamp;
    private String ip;
    private String userAgent;
    private String detalhes;
    private Boolean sucesso;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
```

### AuditLogService

```java
@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository repository;
    
    public void log(String usuario, String acao, String ip, String userAgent, Boolean sucesso, String detalhes) {
        AuditLog log = new AuditLog(
            null, usuario, acao, LocalDateTime.now(), ip, userAgent, detalhes, sucesso
        );
        repository.save(log);
    }
}
```

### Login com Auditoria

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuditLogService auditLogService;
    private final HttpServletRequest request;
    
    public LoginResponse login(LoginRequest request) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException("Email ou senha inválidos"));
            
            if (!passwordEncoder.matches(request.password(), usuario.getSenha())) {
                // Log falha
                auditLogService.log(
                    request.email(),
                    "LOGIN_FALHA",
                    getClientIp(),
                    request.getHeader("User-Agent"),
                    false,
                    "Senha incorreta"
                );
                throw new AuthenticationException("Email ou senha inválidos");
            }
            
            // Log sucesso
            auditLogService.log(
                usuario.getEmail(),
                "LOGIN_SUCESSO",
                getClientIp(),
                request.getHeader("User-Agent"),
                true,
                "Login bem-sucedido"
            );
            
            return buildLoginResponse(usuario);
        } catch (Exception e) {
            auditLogService.log(
                request.email(),
                "LOGIN_ERRO",
                getClientIp(),
                request.getHeader("User-Agent"),
                false,
                e.getMessage()
            );
            throw e;
        }
    }
    
    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return xForwardedFor != null ? xForwardedFor.split(",")[0] : request.getRemoteAddr();
    }
}
```

---

## 6. FRONTEND - IMPLEMENTAR HTTPONLY COOKIES

### Backend - Retornar Cookie

```java
@PostMapping("/auth/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    Usuario usuario = authenticate(request);
    
    String token = jwtService.generateAccessToken(usuario);
    
    // HttpOnly, Secure, SameSite
    ResponseCookie cookie = ResponseCookie
        .from("auth_token", token)
        .httpOnly(true)
        .secure(true)  // HTTPS only
        .path("/")
        .maxAge(Duration.ofMinutes(15))
        .sameSite("Strict")
        .build();
    
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    
    return ResponseEntity.ok(new LoginResponse(usuario));
}
```

### Frontend - axios com credentials

```typescript
// src/services/httpClient.ts
import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,  // Enviar cookies automaticamente
})

export default client
```

---

## 7. FRONTEND - VALIDAR URL DE API

```typescript
// src/config/api.ts
export function getApiUrl(): string {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  
  if (!import.meta.env.DEV) {
    // Produção: forçar HTTPS
    if (!baseUrl.startsWith('https://')) {
      throw new Error('API URL deve usar HTTPS em produção!')
    }
    
    // Validar domínio permitido
    const allowedDomains = [
      'api.extremegym.com',
      'app.extremegym.com'
    ]
    
    try {
      const url = new URL(baseUrl)
      if (!allowedDomains.some(domain => url.hostname.endsWith(domain))) {
        throw new Error(`API URL não autorizada: ${url.hostname}`)
      }
    } catch (e) {
      throw new Error(`URL inválida: ${baseUrl}`)
    }
  }
  
  return baseUrl
}

export const API_URL = getApiUrl()
```

---

## 8. DOCKER COMPOSE - SEGURANÇA

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: extreme-postgres
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - extreme_data:/var/lib/postgresql/data
    networks:
      - extreme-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: extreme-gym-api
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${DB_NAME}
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_USERNAME: ${ADMIN_USERNAME}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    networks:
      - extreme-network
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M

volumes:
  extreme_data:

networks:
  extreme-network:
    driver: bridge
```

### .env.example

```bash
# Database
DB_NAME=extreme_db
DB_USER=extreme_user
DB_PASSWORD=SenhaForte@2024!123

# JWT
JWT_SECRET=seu-secret-256-bit-aleatorio-aqui

# Admin
ADMIN_EMAIL=admin@extremegym.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=SenhaForte@2024!Admin456

# API
VITE_API_BASE_URL=https://api.extremegym.com
```

---

## 9. CI/CD - ADICIONAR SECURITY CHECKS

```yaml
# .github/workflows/security.yml
name: Security Checks

on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload Trivy results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
      
      - name: Run SonarQube
        run: |
          mvn clean verify sonar:sonar \
            -Dsonar.projectKey=extreme-gym-api \
            -Dsonar.sources=src/main
      
      - name: Dependency check
        run: |
          npm audit --audit-level=moderate
```

---

## ✅ RESUMO DE MUDANÇAS

| Arquivo | Mudança | Prioridade |
|---------|---------|-----------|
| pom.xml | Adicionar io.jsonwebtoken | 🔴 CRÍTICO |
| application.properties | Remover defaults de secrets | 🔴 CRÍTICO |
| SecurityConfig.java | Remover flag de segurança | 🔴 CRÍTICO |
| JwtService.java | Implementar refresh token | 🟠 ALTO |
| AuthService.java | Adicionar auditoria | 🟠 ALTO |
| httpClient.ts | Usar cookies HttpOnly | 🟠 ALTO |
| docker-compose.yml | Usar .env | 🟠 ALTO |
| .github/workflows/ | Adicionar security scans | 🟡 MÉDIO |

