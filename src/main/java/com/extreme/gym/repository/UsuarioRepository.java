package com.extreme.gym.repository;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    @Query("""
            select usuario
            from Usuario usuario
            where lower(usuario.email) = lower(:login)
               or lower(usuario.username) = lower(:login)
            """)
    Optional<Usuario> findByEmailOrUsername(String login);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByRole(Role role);
}
