package com.extreme.gym.service;

import com.extreme.gym.dto.auth.LoginRequest;
import com.extreme.gym.dto.auth.LoginResponse;
import com.extreme.gym.dto.auth.RegisterRequest;
import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.repository.UsuarioRepository;
import com.extreme.gym.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.auth.registration-enabled:false}")
    private boolean registrationEnabled;

    public LoginResponse register(RegisterRequest request) {
        if (!registrationEnabled) {
            throw new BusinessException("Registro publico de usuarios esta desabilitado");
        }

        String emailNormalizado = request.email().toLowerCase().trim();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("Usuario ja cadastrado com este email");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(emailNormalizado)
                .username(emailNormalizado)
                .passwordHash(passwordEncoder.encode(request.senha()))
                .role(Role.RECEPCAO)
                .ativo(true)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return buildLoginResponse(salvo);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String loginNormalizado = request.login().toLowerCase().trim();
        
        Usuario usuario = usuarioRepository.findByEmailOrUsername(loginNormalizado)
                .filter(Usuario::getAtivo)
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais invalidas");
        }

        return buildLoginResponse(usuario);
    }

    private LoginResponse buildLoginResponse(Usuario usuario) {
        String token = jwtService.generateToken(usuario);

        return new LoginResponse(
                token,
                TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}
