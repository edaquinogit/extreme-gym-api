package com.extreme.gym.dto.aluno;

import com.extreme.gym.enums.StatusAluno;
import java.time.LocalDateTime;

public record AlunoResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        StatusAluno status,
        LocalDateTime dataCadastro
) {
}
