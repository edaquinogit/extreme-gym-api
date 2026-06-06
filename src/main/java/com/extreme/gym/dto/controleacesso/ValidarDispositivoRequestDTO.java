package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ValidarDispositivoRequestDTO(
        @NotNull(message = "Tipo de credencial e obrigatorio")
        TipoCredencialAcesso credencialTipo,
        @NotBlank(message = "Identificador externo e obrigatorio")
        String identificadorExterno,
        @NotNull(message = "Origem e obrigatoria")
        OrigemEventoAcesso origem,
        @NotBlank(message = "Idempotency key e obrigatoria")
        String idempotencyKey,
        @NotNull(message = "Data/hora do evento e obrigatoria")
        LocalDateTime dataHoraEvento
) {
}
