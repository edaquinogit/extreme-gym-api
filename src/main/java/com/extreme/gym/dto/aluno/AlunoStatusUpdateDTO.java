package com.extreme.gym.dto.aluno;

import com.extreme.gym.enums.StatusAluno;
import jakarta.validation.constraints.NotNull;

public record AlunoStatusUpdateDTO(
        @NotNull(message = "Status e obrigatorio")
        StatusAluno status
) {
}
