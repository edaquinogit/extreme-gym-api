package com.extreme.gym.dto.evento;

public record EventoAcessoLoteItemResponseDTO(
        String idempotencyKey,
        String status,
        Long eventoId,
        String motivo
) {
}
