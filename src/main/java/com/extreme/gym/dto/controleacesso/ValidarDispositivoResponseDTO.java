package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.ResultadoAcesso;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ValidarDispositivoResponseDTO(
        Boolean permitido,
        ResultadoAcesso resultado,
        String motivo,
        AlunoResumo aluno,
        MatriculaResumo matricula,
        PlanoResumo plano,
        DispositivoResumo dispositivo,
        Long eventoId,
        LocalDateTime dataHora
) {

    public record AlunoResumo(Long id, String nome) {
    }

    public record MatriculaResumo(Long id, LocalDate dataValidade) {
    }

    public record PlanoResumo(Long id, String nome) {
    }

    public record DispositivoResumo(Long id, String nome, String status) {
    }
}
