package com.extreme.gym.dto.matricula;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MatriculaRequestDTO(
        @NotNull(message = "Aluno e obrigatorio")
        Long alunoId,

        @NotNull(message = "Plano e obrigatorio")
        Long planoId,

        @NotNull(message = "Data de inicio e obrigatoria")
        LocalDate dataInicio
) {
}
