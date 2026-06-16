package com.extreme.gym.config;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class AdminUserInitializerTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCriarAdminInicialComSenhaBCryptSemDuplicar() throws Exception {
        AdminUserInitializer initializer = new AdminUserInitializer(
                usuarioRepository,
                passwordEncoder,
                true,
                "admin@extremegym.com",
                "admin",
                "admin123"
        );

        initializer.run();
        initializer.run();

        Usuario admin = usuarioRepository.findByEmail("admin@extremegym.com").orElseThrow();
        assertEquals(1, usuarioRepository.count());
        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertNotEquals("admin123", admin.getPasswordHash());
        assertTrue(passwordEncoder.matches("admin123", admin.getPasswordHash()));
    }

    @Test
    void naoDeveCriarAdminQuandoBootstrapEstiverDesabilitado() throws Exception {
        AdminUserInitializer initializer = new AdminUserInitializer(
                usuarioRepository,
                passwordEncoder,
                false,
                "admin@extremegym.com",
                "admin",
                "admin123"
        );

        initializer.run();

        assertEquals(0, usuarioRepository.count());
    }

    @Test
    void naoDeveDuplicarQuandoJaExistirAdminComOutroLogin() throws Exception {
        usuarioRepository.save(Usuario.builder()
                .nome("Admin Existente")
                .email("admin-existente@extremegym.com")
                .username("admin-existente")
                .passwordHash(passwordEncoder.encode("senha-segura"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        AdminUserInitializer initializer = new AdminUserInitializer(
                usuarioRepository,
                passwordEncoder,
                true,
                "novo-admin@extremegym.com",
                "novo-admin",
                "admin123"
        );

        initializer.run();

        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void deveFalharSeBootstrapHabilitadoSemSenha() {
        AdminUserInitializer initializer = new AdminUserInitializer(
                usuarioRepository,
                passwordEncoder,
                true,
                "admin@extremegym.com",
                "admin",
                ""
        );

        assertThrows(IllegalStateException.class, initializer::run);
    }
}
