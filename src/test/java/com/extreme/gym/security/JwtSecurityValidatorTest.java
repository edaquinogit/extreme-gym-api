package com.extreme.gym.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("JWT Security Validator Tests")
class JwtSecurityValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of());

    @Nested
    @DisplayName("Validação de JWT Secret")
    class JwtSecretValidationTests {

        @Test
        @DisplayName("Deve falhar se JWT_SECRET não estiver configurado")
        void shouldFailWhenJwtSecretIsNotConfigured() {
            contextRunner
                    .withPropertyValues("spring.profiles.active=dev")
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        assertTrue(context.getStartupFailure() != null,
                            "Esperava falha no startup por configuração JWT inválida");
                    });
        }

        @Test
        @DisplayName("Deve falhar se JWT_SECRET contiver padrão inseguro 'dev-secret'")
        void shouldFailWhenJwtSecretContainsInsecurePattern() {
            contextRunner
                    .withPropertyValues(
                            "spring.profiles.active=dev",
                            "jwt.secret=dev-secret-change-me"
                    )
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        assertTrue(context.getStartupFailure() != null,
                            "Esperava falha no startup por configuração JWT inválida");
                    });
        }

        @Test
        @DisplayName("Deve falhar se JWT_SECRET for muito curto (< 32 caracteres)")
        void shouldFailWhenJwtSecretIsTooShort() {
            contextRunner
                    .withPropertyValues(
                            "spring.profiles.active=dev",
                            "jwt.secret=shortsecret"
                    )
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        assertTrue(context.getStartupFailure() != null,
                            "Esperava falha no startup por configuração JWT inválida");
                    });
        }

        @Test
        @DisplayName("Deve falhar para secret padrão 'test-secret' em ambiente dev")
        void shouldFailForTestSecretPatternInNonTestEnvironment() {
            contextRunner
                    .withPropertyValues(
                            "spring.profiles.active=dev",
                            "jwt.secret=test-secret-12345678901234567890"
                    )
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        assertTrue(context.getStartupFailure() != null,
                            "Esperava falha no startup por configuração JWT inválida");
                    });
        }

        @Test
        @DisplayName("Deve aceitar JWT_SECRET com comprimento mínimo (32 chars) e padrão seguro")
        void shouldAcceptValidJwtSecret() {
            String validSecret = "MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==";

            contextRunner
                    .withPropertyValues(
                            "spring.profiles.active=dev",
                            "jwt.secret=" + validSecret
                    )
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        assertDoesNotThrow(() -> context.getBean(JwtSecurityValidator.class));
                        assertNotNull(context.getBean(JwtSecurityValidator.class));
                    });
        }
    }

    @Nested
    @DisplayName("Teste específico para aplicação no perfil 'test'")
    class TestProfileTests {

        @Test
        @DisplayName("Deve permitir 'test-secret' APENAS no profile 'test'")
        void shouldAllowTestSecretOnlyInTestProfile() {
            String testSecret = "test-secret-1234567890123456789012";

            contextRunner
                    .withPropertyValues(
                            "spring.profiles.active=test",
                            "jwt.secret=" + testSecret
                    )
                    .withUserConfiguration(JwtSecurityValidator.class)
                    .run(context -> {
                        // Em teste, permitimos test-secret (isolado em application-test.properties)
                        assertDoesNotThrow(() -> context.getBean(JwtSecurityValidator.class));
                    });
        }
    }
}
