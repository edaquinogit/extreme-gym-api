package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.TipoCredencialAcesso;
import java.time.LocalDateTime;
import java.util.List;

public record SnapshotAutorizadosResponseDTO(
        String versaoSnapshot,
        LocalDateTime geradoEm,
        LocalDateTime validoAte,
        long totalCredenciais,
        long totalLiberados,
        long totalBloqueados,
        List<Item> itens
) {

    public record Item(
            Long alunoId,
            TipoCredencialAcesso credencialTipo,
            String identificadorExterno,
            Boolean liberado,
            String motivoBloqueio,
            LocalDateTime validoAte,
            LocalDateTime atualizadoEm
    ) {
    }
}
