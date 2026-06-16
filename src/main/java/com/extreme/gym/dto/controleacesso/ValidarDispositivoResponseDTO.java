package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.ResultadoAcesso;
import java.time.LocalDateTime;

public record ValidarDispositivoResponseDTO(
        boolean permitido,
        ResultadoAcesso resultado,
        String motivo,
        Long alunoId,
        String alunoNome,
        Long eventoId,
        LocalDateTime dataHora
) {
}
