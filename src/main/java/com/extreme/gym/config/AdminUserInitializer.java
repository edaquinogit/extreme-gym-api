package com.extreme.gym.config;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.username:}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            log.debug("Bootstrap de administrador inicial desabilitado.");
            return;
        }

        validateBootstrapConfiguration();

        if (usuarioRepository.existsByRole(Role.ADMIN)) {
            log.info("Bootstrap de administrador ignorado: ja existe usuario ADMIN.");
            return;
        }

        if (usuarioRepository.existsByEmail(adminEmail) || usuarioRepository.existsByUsername(adminUsername)) {
            log.warn("Bootstrap de administrador ignorado: email ou username ja existe sem role ADMIN.");
            return;
        }

        Usuario admin = Usuario.builder()
                .nome("Administrador")
                .email(adminEmail)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .ativo(true)
                .build();

        usuarioRepository.save(admin);
        log.info("Usuario ADMIN inicial criado com username='{}' e email='{}'. Desabilite o bootstrap apos o primeiro uso.", adminUsername, adminEmail);
    }

    private void validateBootstrapConfiguration() {
        if (isBlank(adminEmail) || isBlank(adminUsername) || isBlank(adminPassword)) {
            throw new IllegalStateException("Bootstrap ADMIN habilitado requer app.admin.email, app.admin.username e app.admin.password.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
