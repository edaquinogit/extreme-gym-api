package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import java.time.LocalDateTime;

public record DispositivoHeartbeatRequestDTO(
        String gatewayId,
        LocalDateTime timestamp,
        StatusDispositivoAcesso status,
        ModoOperacaoDispositivo modoOperacao,
        Integer pendingEvents,
        String version
) {
}
