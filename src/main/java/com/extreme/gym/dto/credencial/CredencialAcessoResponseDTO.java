package com.extreme.gym.dto.credencial;

import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import java.time.LocalDateTime;

public record CredencialAcessoResponseDTO(
        Long id,
        Long alunoId,
        TipoCredencialAcesso tipo,
        String identificadorExterno,
        String fornecedor,
        StatusCredencialAcesso status,
        LocalDateTime cadastradoEm,
        LocalDateTime revogadoEm,
        LocalDateTime termoAceitoEm,
        String versaoTermo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
