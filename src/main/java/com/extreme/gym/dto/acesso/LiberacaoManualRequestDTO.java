package com.extreme.gym.dto.acesso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LiberacaoManualRequestDTO(
        @NotNull(message = "Aluno e obrigatorio")
        Long alunoId,
        @NotBlank(message = "Motivo e obrigatorio")
        String motivo,
        String observacao,
        Boolean registrarCheckin
) {
}
