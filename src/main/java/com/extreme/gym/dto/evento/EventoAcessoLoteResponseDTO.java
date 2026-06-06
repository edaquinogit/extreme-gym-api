package com.extreme.gym.dto.evento;

import java.util.List;

public record EventoAcessoLoteResponseDTO(
        int totalRecebidos,
        int totalCriados,
        int totalDuplicados,
        int totalRejeitados,
        List<EventoAcessoLoteItemResponseDTO> detalhes
) {
}
