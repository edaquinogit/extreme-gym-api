package com.extreme.gym.dto.evento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EventoAcessoLoteRequestDTO(
        @NotEmpty(message = "Eventos sao obrigatorios")
        List<@Valid EventoAcessoRequestDTO> eventos
) {
}
