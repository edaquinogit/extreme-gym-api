package com.extreme.gym.dto.controleacesso;

import java.time.LocalDateTime;
import java.util.List;

public record SnapshotAutorizadosResponseDTO(
        String versaoSnapshot,
        LocalDateTime geradoEm,
        LocalDateTime validoAte,
        int totalCredenciais,
        int totalLiberados,
        int totalBloqueados,
        List<SnapshotAutorizadoItemDTO> itens
) {
}
