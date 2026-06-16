package com.extreme.gym.dto.auth;

import com.extreme.gym.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthDtoMaskingTest {

    @Test
    void loginRequestToStringNaoDeveExporSenha() {
        LoginRequest request = new LoginRequest("admin", "senha-super-secreta");

        String output = request.toString();

        assertTrue(output.contains("password=***"));
        assertFalse(output.contains("senha-super-secreta"));
    }

    @Test
    void loginResponseToStringNaoDeveExporToken() {
        LoginResponse response = new LoginResponse(
                "jwt-super-secreto",
                "Bearer",
                3600L,
                1L,
                "Admin",
                "admin",
                "admin@extremegym.local",
                Role.ADMIN
        );

        String output = response.toString();

        assertTrue(output.contains("token=***"));
        assertFalse(output.contains("jwt-super-secreto"));
    }
}
