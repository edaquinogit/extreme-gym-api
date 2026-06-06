package com.extreme.gym.dto.evento;

import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import java.time.LocalDateTime;

public record EventoAcessoResponseDTO(
        Long id,
        Long alunoId,
        Long dispositivoId,
        Long matriculaId,
        OrigemEventoAcesso origem,
        ModoEventoAcesso modo,
        ResultadoAcesso resultado,
        String motivo,
        LocalDateTime dataHoraEvento,
        LocalDateTime dataHoraRecebimento,
        String idempotencyKey
) {
}
