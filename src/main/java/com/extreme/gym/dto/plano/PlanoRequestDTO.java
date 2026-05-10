package com.extreme.gym.dto.plano;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PlanoRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 3, max = 80, message = "Nome deve ter entre 3 e 80 caracteres")
        String nome,

        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "Valor mensal e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor mensal deve ser maior que zero")
        BigDecimal valorMensal,

        @NotNull(message = "Duracao em dias e obrigatoria")
        @Positive(message = "Duracao em dias deve ser maior que zero")
        Integer duracaoEmDias
) {
}
