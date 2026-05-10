package com.extreme.gym.dto.plano;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorMensal,
        Integer duracaoEmDias,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
}
