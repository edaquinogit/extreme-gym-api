package com.extreme.gym.dto.usuario;

import com.extreme.gym.enums.Role;
import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String username,
        Role role,
        Boolean ativo,
        LocalDateTime criadoEm
) {
}
