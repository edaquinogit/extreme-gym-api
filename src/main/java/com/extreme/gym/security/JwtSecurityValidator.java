package com.extreme.gym.security;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Valida a configuração JWT no startup.
 *
 * <p>Princípios: - Fail-fast: detecta configuração insegura no startup
 * - Clear & Explicit: mensagens de erro claras e actionáveis - Environment-aware:
 * requerimentos diferentes por profile - Secure-by-default: rejeita secrets fracos
 *
 * <p>Vulnerabilidades prevenidas: - JWT Secret falsificação via secret fraco - Uso
 * acidental de valores default em produção - Secrets commitadas no repositório
 *
 * @since 1.0
 */
@Component
public class JwtSecurityValidator {

    private static final int MINIMUM_SECRET_LENGTH = 32;
    private static final String DEV_SECRET_WARNING = "dev-secret";
    private static final String TEST_SECRET = "test-secret";

    private final String secret;
    private final String activeProfile;

    public JwtSecurityValidator(
            @Value("${jwt.secret}") String secret,
            @Value("${spring.profiles.active:default}") String activeProfile
    ) {
        this.secret = secret;
        this.activeProfile = activeProfile;
        validateJwtSecurityConfiguration();
    }

    private void validateJwtSecurityConfiguration() {
        // 1. Verificar se secret está vazio/null
        if (secret == null || secret.isBlank()) {
            throwConfigurationException(
                    "JWT_SECRET não configurado",
                    "Exporte a variável de ambiente JWT_SECRET com um valor seguro.",
                    "export JWT_SECRET=\"$(openssl rand -base64 32)\""
            );
        }

        // 2. Detectar secrets padrão/fraco do projeto
        // Profile test pode usar prefixo 'test-' como exceção controlada
        boolean isTestSecretInTestProfile = isTestProfile() && secret.startsWith("test-");
        if (!isTestSecretInTestProfile && containsInsecurePattern(secret)) {
            throwConfigurationException(
                    "JWT Secret detectado como inseguro (padrão do projeto)",
                    "O secret contém valores padrão como 'dev-secret' ou 'test-secret'.",
                    "export JWT_SECRET=\"$(openssl rand -base64 32)\""
            );
        }

        // 3. Validar comprimento mínimo
        if (secret.length() < MINIMUM_SECRET_LENGTH) {
            throwConfigurationException(
                    String.format(
                            "JWT Secret muito curto (%d chars, esperado: %d)",
                            secret.length(),
                            MINIMUM_SECRET_LENGTH
                    ),
                    "Um secret fraco permite ataques de força bruta.",
                    "export JWT_SECRET=\"$(openssl rand -base64 32)\""
            );
        }

        // 4. Warning se em ambiente de teste
        if (isTestProfile() && secret.startsWith("test-")) {
            // Permitido apenas em testes
            logTestEnvironmentWarning();
        }

        // 5. Warning se em ambiente dev com secret customizado
        if (isDevProfile() && !secret.startsWith("dev-")) {
            logProductionSecretInDev();
        }
    }

    private boolean containsInsecurePattern(String value) {
        String lowercaseValue = value.toLowerCase();
        return lowercaseValue.contains(DEV_SECRET_WARNING)
                || lowercaseValue.contains(TEST_SECRET)
                || lowercaseValue.equals("secret")
                || lowercaseValue.equals("12345678");
    }

    private boolean isTestProfile() {
        return "test".equals(activeProfile);
    }

    private boolean isDevProfile() {
        return activeProfile.contains("dev") || activeProfile.contains("local");
    }

    private void throwConfigurationException(
            String title,
            String description,
            String suggestion
    ) {
        String message = String.format(
                """
                ╔════════════════════════════════════════════════════════════╗
                ║ 🔴 ERRO DE CONFIGURAÇÃO JWT - Falha na Inicialização      ║
                ╠════════════════════════════════════════════════════════════╣
                ║ %s
                ║ %s
                ║
                ║ Sugestão:
                ║ %s
                ╚════════════════════════════════════════════════════════════╝
                """,
                padString(title, 56),
                padString(description, 56),
                padString(suggestion, 56)
        );

        throw new JwtSecurityConfigurationException(message);
    }

    private void logTestEnvironmentWarning() {
        // Em testes, o secret é configurado no application-test.properties
        // Esta é uma situação aceitável e isolada
    }

    private void logProductionSecretInDev() {
        // Silenciosamente permitido - pode estar usando uma chave real para dev
    }

    private String padString(String text, int maxWidth) {
        if (text.length() <= maxWidth - 4) {
            return text + " ".repeat(maxWidth - text.length() - 4);
        }
        return text.substring(0, maxWidth - 7) + "...";
    }

    /**
     * Exception customizada para erros de configuração JWT.
     * Distingui erros de segurança de outros tipos de erro.
     */
    static class JwtSecurityConfigurationException extends RuntimeException {
        JwtSecurityConfigurationException(String message) {
            super(message);
        }
    }
}
