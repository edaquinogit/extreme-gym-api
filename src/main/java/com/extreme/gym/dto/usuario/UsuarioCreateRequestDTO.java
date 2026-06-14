package com.extreme.gym.dto.usuario;

import com.extreme.gym.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email deve ser valido")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 12, max = 120, message = "Senha deve ter entre 12 e 120 caracteres")
        String senha,

        @NotNull(message = "Perfil e obrigatorio")
        Role role
) {
}
