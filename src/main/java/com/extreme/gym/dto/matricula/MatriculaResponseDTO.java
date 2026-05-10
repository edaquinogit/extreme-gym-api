package com.extreme.gym.dto.matricula;

import com.extreme.gym.enums.StatusMatricula;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatriculaResponseDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long planoId,
        String planoNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusMatricula status,
        LocalDateTime dataCadastro
) {
}
