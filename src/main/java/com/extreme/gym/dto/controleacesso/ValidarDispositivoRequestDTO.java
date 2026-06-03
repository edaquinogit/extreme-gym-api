package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ValidarDispositivoRequestDTO(
        @NotNull(message = "Dispositivo e obrigatorio")
        Long dispositivoId,
        @NotNull(message = "Tipo de credencial e obrigatorio")
        TipoCredencialAcesso credencialTipo,
        @NotBlank(message = "Identificador externo e obrigatorio")
        String identificadorExterno,
        @NotNull(message = "Origem e obrigatoria")
        OrigemEventoAcesso origem,
        String idempotencyKey,
        LocalDateTime dataHoraEvento
) {
}
