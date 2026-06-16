package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DispositivoAcessoRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        String nome,
        @NotNull(message = "Tipo e obrigatorio")
        TipoDispositivoAcesso tipo,
        @NotNull(message = "Modo de operacao e obrigatorio")
        ModoOperacaoDispositivo modoOperacao,
        String identificadorExterno,
        String fabricante,
        String modelo,
        String ipLocal,
        String unidade
) {
}
