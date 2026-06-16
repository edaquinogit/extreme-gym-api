package com.extreme.gym.dto.acesso;

public record AssistenciaPinResponseDTO(
        Long alunoId,
        String alunoNome,
        boolean valido
) {
}
