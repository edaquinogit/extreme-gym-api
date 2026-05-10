package com.extreme.gym.dto.pagamento;

import com.extreme.gym.enums.FormaPagamento;
import com.extreme.gym.enums.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long id,
        Long matriculaId,
        Long alunoId,
        String alunoNome,
        Long planoId,
        String planoNome,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        StatusPagamento status,
        LocalDateTime dataPagamento,
        LocalDateTime dataCadastro
) {
}
