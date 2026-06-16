package com.extreme.gym.dto.evento;

import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventoAcessoRequestDTO(
        Long alunoId,
        @NotNull(message = "Dispositivo e obrigatorio")
        Long dispositivoId,
        Long matriculaId,
        @NotNull(message = "Origem e obrigatoria")
        OrigemEventoAcesso origem,
        @NotNull(message = "Modo e obrigatorio")
        ModoEventoAcesso modo,
        @NotNull(message = "Resultado e obrigatorio")
        ResultadoAcesso resultado,
        @NotBlank(message = "Motivo e obrigatorio")
        String motivo,
        LocalDateTime dataHoraEvento,
        Boolean sincronizado,
        String identificadorExternoEvento,
        String idempotencyKey
) {
}
