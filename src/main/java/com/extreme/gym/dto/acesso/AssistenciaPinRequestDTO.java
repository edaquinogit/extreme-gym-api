package com.extreme.gym.dto.acesso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssistenciaPinRequestDTO(
        @NotNull(message = "Aluno e obrigatorio")
        Long alunoId,
        @NotBlank(message = "PIN e obrigatorio")
        String pin
) {
}
