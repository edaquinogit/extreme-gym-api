package com.extreme.gym.config;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev | local")
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    private static final int GENERATED_PASSWORD_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@extremegym.local}") String adminEmail,
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(adminEmail) || usuarioRepository.existsByUsername(adminUsername)) {
            return;
        }

        String passwordToUse = resolveAdminPassword();
        Usuario admin = Usuario.builder()
                .nome("Administrador")
                .email(adminEmail)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(passwordToUse))
                .role(Role.ADMIN)
                .ativo(true)
                .build();

        usuarioRepository.save(admin);

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("Nenhuma senha administrativa configurada. Usuario '{}' criado com credenciais temporarias. Defina ADMIN_PASSWORD imediatamente.", adminUsername);
            log.info("Admin temporary credentials: email='{}', username='{}', password='{}'", adminEmail, adminUsername, passwordToUse);
            return;
        }

        log.info("Usuario administrador local criado: email='{}', username='{}'", adminEmail, adminUsername);
    }

    private String resolveAdminPassword() {
        if (adminPassword == null || adminPassword.isBlank()) {
            return generateSecurePassword();
        }
        return adminPassword;
    }

    private String generateSecurePassword() {
        return IntStream.range(0, GENERATED_PASSWORD_LENGTH)
                .map(i -> PASSWORD_CHARACTERS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARACTERS.length())))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
