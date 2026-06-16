package com.extreme.gym.dto.buscaglobal;

import java.util.List;
import java.util.Map;

public record BuscaGlobalResponseDTO(
        List<Item> alunos,
        List<Item> matriculas,
        List<Item> pagamentos,
        List<Item> planos
) {

    public record Item(
            Long id,
            String tipo,
            String titulo,
            String subtitulo,
            String status,
            String rota,
            Map<String, Object> metadata
    ) {
    }
}
