package com.extreme.gym.dto.pagamento;

import com.extreme.gym.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PagamentoRequestDTO(
        @NotNull(message = "Matricula e obrigatoria")
        Long matriculaId,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Forma de pagamento e obrigatoria")
        FormaPagamento formaPagamento
) {
}
