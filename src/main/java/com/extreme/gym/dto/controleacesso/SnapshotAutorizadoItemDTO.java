package com.extreme.gym.dto.controleacesso;

import com.extreme.gym.enums.TipoCredencialAcesso;
import java.time.LocalDateTime;

public record SnapshotAutorizadoItemDTO(
        Long alunoId,
        TipoCredencialAcesso credencialTipo,
        String identificadorExterno,
        boolean liberado,
        String motivoBloqueio,
        LocalDateTime validoAte,
        LocalDateTime atualizadoEm
) {
}
