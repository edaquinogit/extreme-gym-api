package com.extreme.gym.dto.dispositivo;

public record DispositivoAcessoCreatedResponseDTO(
        DispositivoAcessoResponseDTO dispositivo,
        String apiKeyPlaintext
) {
}
