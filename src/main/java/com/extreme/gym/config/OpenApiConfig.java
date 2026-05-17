package com.extreme.gym.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI extremeGymOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Extreme Gym API")
                        .description("""
                                API REST para gestão de academia, controle de alunos, planos, \
                                matrículas, pagamentos, check-ins e validação de acesso.

                                Autenticação: use `POST /auth/login` com credenciais válidas. \
                                O contrato e exemplos de payload estão descritos nesta documentação.
                                """)
                        .version("1.0.0"));
    }
}
