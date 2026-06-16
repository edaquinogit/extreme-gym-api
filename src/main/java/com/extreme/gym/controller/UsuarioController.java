package com.extreme.gym.controller;

import com.extreme.gym.dto.usuario.UsuarioCreateRequestDTO;
import com.extreme.gym.dto.usuario.UsuarioResponseDTO;
import com.extreme.gym.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestao de usuarios do sistema (apenas ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCreateRequestDTO request) {
        UsuarioResponseDTO criado = usuarioService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<UsuarioResponseDTO> alterarAtivo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Boolean ativo = body.get("ativo");
        if (ativo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(usuarioService.alterarAtivo(id, ativo));
    }
}
