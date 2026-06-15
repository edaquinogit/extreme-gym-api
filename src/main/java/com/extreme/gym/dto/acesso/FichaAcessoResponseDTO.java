package com.extreme.gym.dto.acesso;

import com.extreme.gym.enums.FormaPagamento;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.enums.TipoCredencialAcesso;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FichaAcessoResponseDTO(
        AlunoResumoDTO aluno,
        MatriculaResumoDTO matriculaAtiva,
        PagamentoResumoDTO ultimoPagamento,
        List<CredencialResumoDTO> credenciaisAtivas,
        List<String> restricoes,
        List<String> acoesPermitidas
) {

    public record AlunoResumoDTO(
            Long id,
            String nome,
            String email,
            String telefone,
            StatusAluno status,
            LocalDateTime dataCadastro
    ) {
    }

    public record MatriculaResumoDTO(
            Long id,
            Long planoId,
            String planoNome,
            LocalDate dataInicio,
            LocalDate dataFim,
            StatusMatricula status
    ) {
    }

    public record PagamentoResumoDTO(
            Long id,
            Long matriculaId,
            BigDecimal valor,
            FormaPagamento formaPagamento,
            StatusPagamento status,
            LocalDateTime dataPagamento,
            LocalDateTime dataCadastro
    ) {
    }

    public record CredencialResumoDTO(
            Long id,
            TipoCredencialAcesso tipo,
            String fornecedor,
            StatusCredencialAcesso status,
            LocalDateTime cadastradoEm
    ) {
    }
}
