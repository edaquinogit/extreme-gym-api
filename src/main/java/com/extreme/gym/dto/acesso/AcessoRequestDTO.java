package com.extreme.gym.dto.acesso;

import jakarta.validation.constraints.NotNull;

public record AcessoRequestDTO(
        @NotNull(message = "Aluno e obrigatorio")
        Long alunoId
) {
}
