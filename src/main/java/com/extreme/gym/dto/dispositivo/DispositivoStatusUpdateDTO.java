package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.StatusDispositivoAcesso;
import jakarta.validation.constraints.NotNull;

public record DispositivoStatusUpdateDTO(
        @NotNull(message = "Status e obrigatorio")
        StatusDispositivoAcesso status
) {
}
