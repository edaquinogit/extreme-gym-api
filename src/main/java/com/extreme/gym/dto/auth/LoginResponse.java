package com.extreme.gym.dto.auth;

import com.extreme.gym.enums.Role;

public record LoginResponse(
        String token,
        String type,
        Long expiresInSeconds,
        Long usuarioId,
        String nome,
        String username,
        String email,
        Role role
) {
}
