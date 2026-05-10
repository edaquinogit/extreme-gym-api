package com.extreme.gym.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlunoRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 3, message = "Nome deve ter no minimo 3 caracteres")
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email deve ser valido")
        String email,

        @NotBlank(message = "Telefone e obrigatorio")
        @Size(min = 8, max = 15, message = "Telefone deve ter entre 8 e 15 caracteres")
        String telefone
) {
}
