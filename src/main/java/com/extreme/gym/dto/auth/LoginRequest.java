package com.extreme.gym.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de autenticacao")
public record LoginRequest(
        @JsonAlias("email")
        @NotBlank(message = "Usuario ou email e obrigatorio")
        @Schema(
                description = "Email ou username do usuario",
                example = "usuario@exemplo.com"
        )
        String username,

        @NotBlank(message = "Senha e obrigatoria")
        @JsonAlias("senha")
        @Schema(
                description = "Senha do usuario",
                example = "sua-senha",
                format = "password"
        )
        String password
) {
    public String login() {
        return username;
    }
}
