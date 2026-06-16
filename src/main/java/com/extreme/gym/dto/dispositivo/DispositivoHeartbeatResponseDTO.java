package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.StatusDispositivoAcesso;
import java.time.LocalDateTime;

public record DispositivoHeartbeatResponseDTO(
        Long dispositivoId,
        StatusDispositivoAcesso status,
        LocalDateTime ultimaComunicacaoEm,
        boolean accepted
) {
}
