package com.extreme.gym.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI extremeGymOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Extreme Gym API")
                        .description("API REST para gestão de academia, controle de alunos, planos, matrículas, pagamentos, check-ins e validação de acesso.")
                        .version("1.0.0"));
    }
}
