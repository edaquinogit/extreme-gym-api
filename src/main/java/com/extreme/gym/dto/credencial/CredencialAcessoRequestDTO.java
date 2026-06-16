package com.extreme.gym.dto.credencial;

import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CredencialAcessoRequestDTO(
        @NotNull(message = "Tipo e obrigatorio")
        TipoCredencialAcesso tipo,
        @NotBlank(message = "Identificador externo e obrigatorio")
        String identificadorExterno,
        String fornecedor,
        StatusCredencialAcesso status,
        LocalDateTime termoAceitoEm,
        String versaoTermo
) {
}
