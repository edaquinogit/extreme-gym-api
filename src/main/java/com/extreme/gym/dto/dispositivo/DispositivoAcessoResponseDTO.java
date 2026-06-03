package com.extreme.gym.dto.dispositivo;

import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import java.time.LocalDateTime;

public record DispositivoAcessoResponseDTO(
        Long id,
        String nome,
        TipoDispositivoAcesso tipo,
        String fabricante,
        String modelo,
        String identificadorExterno,
        String ipLocal,
        String unidade,
        StatusDispositivoAcesso status,
        ModoOperacaoDispositivo modoOperacao,
        LocalDateTime ultimaComunicacaoEm,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
