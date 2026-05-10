package com.extreme.gym.dto.acesso;

import java.time.LocalDate;

public record AcessoResponseDTO(
        Long alunoId,
        String alunoNome,
        Boolean acessoLiberado,
        String motivo,
        Long matriculaId,
        LocalDate dataValidadeMatricula
) {
}
