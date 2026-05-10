package com.extreme.gym.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AlunoRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email deve ser valido")
        String email,

        @NotBlank(message = "Telefone e obrigatorio")
        String telefone
) {
}
