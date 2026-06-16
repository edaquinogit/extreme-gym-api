package com.extreme.gym.service;

import com.extreme.gym.dto.usuario.UsuarioCreateRequestDTO;
import com.extreme.gym.dto.usuario.UsuarioResponseDTO;
import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ja existe usuario cadastrado com este email");
        }

        String username = request.email().toLowerCase();
        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessException("Ja existe usuario com este username");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email().toLowerCase())
                .username(username)
                .passwordHash(passwordEncoder.encode(request.senha()))
                .role(request.role())
                .ativo(true)
                .build();

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO alterarAtivo(Long id, boolean ativo) {
        Usuario usuario = buscarEntidadePorId(id);

        if (!ativo && usuario.getRole() == Role.ADMIN) {
            long adminsAtivos = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.getAtivo() && !u.getId().equals(id))
                    .count();
            if (adminsAtivos == 0) {
                throw new BusinessException("Nao e possivel desativar o unico administrador ativo");
            }
        }

        usuario.setAtivo(ativo);
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado com id: " + id));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getUsername(),
                usuario.getRole(),
                usuario.getAtivo(),
                usuario.getCriadoEm()
        );
    }
}
