package com.extreme.gym.dto.evento;

import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventoAcessoRequestDTO(
        @NotBlank(message = "Idempotency key e obrigatoria")
        String idempotencyKey,
        Long alunoId,
        TipoCredencialAcesso credencialTipo,
        String identificadorExterno,
        @NotNull(message = "Resultado e obrigatorio")
        ResultadoAcesso resultado,
        @NotBlank(message = "Motivo e obrigatorio")
        String motivo,
        @NotNull(message = "Modo e obrigatorio")
        ModoEventoAcesso modo,
        @NotNull(message = "Origem e obrigatoria")
        OrigemEventoAcesso origem,
        @NotNull(message = "Data/hora do evento e obrigatoria")
        LocalDateTime dataHoraEvento,
        String identificadorExternoEvento
) {
}
