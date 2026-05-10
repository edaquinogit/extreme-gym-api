package com.extreme.gym.dto.checkin;

import java.time.LocalDateTime;

public record CheckInResponseDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long matriculaId,
        Boolean permitido,
        String motivo,
        LocalDateTime dataHora
) {
}
