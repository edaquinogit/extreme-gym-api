package com.extreme.gym.dto.checkin;

import jakarta.validation.constraints.NotNull;

public record CheckInRequestDTO(
        @NotNull(message = "Aluno e obrigatorio")
        Long alunoId
) {
}
