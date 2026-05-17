package com.extreme.gym.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @JsonAlias("email")
        @NotBlank(message = "Usuario ou email e obrigatorio")
        String username,

        @NotBlank(message = "Senha e obrigatoria")
        @JsonAlias("senha")
        String password
) {
    public String login() {
        return username;
    }
}
