package com.extreme.gym.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.enabled:true}") boolean securityEnabled
    ) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nao autenticado"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado")));

        if (!securityEnabled && isTestProfile()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> {
                // Allow public access to SPA entry and static assets so browser
                // navigation to client routes works without authentication.
                auth.requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg", "/icons.svg", "/auth/**").permitAll();
                    if (isSwaggerAccessible()) {
                        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    } else {
                        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").hasRole("ADMIN");
                    }
                    auth.requestMatchers("/usuarios/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/alunos/**").hasAnyRole("ADMIN", "RECEPCAO", "PROFESSOR");
                    auth.requestMatchers("/alunos/**").hasAnyRole("ADMIN", "RECEPCAO");
                    auth.requestMatchers("/planos/**").hasAnyRole("ADMIN", "RECEPCAO");
                    auth.requestMatchers("/matriculas/**").hasAnyRole("ADMIN", "RECEPCAO");
                    auth.requestMatchers("/pagamentos/**").hasAnyRole("ADMIN", "RECEPCAO");
                    auth.requestMatchers(HttpMethod.GET, "/checkins/**").hasAnyRole("ADMIN", "RECEPCAO", "PROFESSOR");
                    auth.requestMatchers("/checkins/**").hasAnyRole("ADMIN", "RECEPCAO", "CATRACA");
                    auth.requestMatchers("/acessos/**").hasAnyRole("ADMIN", "RECEPCAO", "CATRACA");
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private boolean isTestProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("test".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSwaggerAccessible() {
        return environment.acceptsProfiles(Profiles.of("dev", "local"));
    }
}
