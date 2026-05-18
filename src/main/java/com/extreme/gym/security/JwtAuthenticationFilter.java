package com.extreme.gym.security;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        jwtService.validateToken(token).ifPresent(jwtUser -> {
            // Verifica se o usuario ainda existe e esta ativo no banco
            usuarioRepository.findByEmail(jwtUser.email())
                    .filter(Usuario::getAtivo)
                    .ifPresent(usuario -> {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                jwtUser.email(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + jwtUser.role().name()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        });

        filterChain.doFilter(request, response);
    }
}
