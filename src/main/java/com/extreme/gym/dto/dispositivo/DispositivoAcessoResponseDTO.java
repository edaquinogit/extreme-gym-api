package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import java.time.LocalDateTime;

public record DispositivoAcessoResponseDTO(
        Long id,
        String nome,
        TipoDispositivoAcesso tipo,
        StatusDispositivoAcesso status,
        ModoOperacaoDispositivo modoOperacao,
        String identificadorExterno,
        String fabricante,
        String modelo,
        String ipLocal,
        String unidade,
        LocalDateTime ultimaComunicacaoEm,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
