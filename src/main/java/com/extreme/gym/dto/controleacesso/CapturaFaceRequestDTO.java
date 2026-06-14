package com.extreme.gym.dto.controleacesso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CapturaFaceRequestDTO(
        @NotNull(message = "ID do aluno e obrigatorio")
        Long alunoId,
        @NotBlank(message = "Template facial e obrigatorio")
        String faceTemplate
) {
}
