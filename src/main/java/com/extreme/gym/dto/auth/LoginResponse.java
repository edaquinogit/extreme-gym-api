package com.extreme.gym.dto.auth;

import com.extreme.gym.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticacao com token JWT")
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        @Schema(example = "Bearer")
        String type,
        @Schema(example = "3600")
        Long expiresInSeconds,
        @Schema(example = "1")
        Long usuarioId,
        @Schema(example = "Usuario Exemplo")
        String nome,
        @Schema(example = "usuario")
        String username,
        @Schema(example = "usuario@exemplo.com")
        String email,
        @Schema(example = "RECEPCAO")
        Role role
) {
    @Override
    public String toString() {
        return "LoginResponse[token=***, type=" + type
                + ", expiresInSeconds=" + expiresInSeconds
                + ", usuarioId=" + usuarioId
                + ", nome=" + nome
                + ", username=" + username
                + ", email=" + email
                + ", role=" + role
                + "]";
    }
}
