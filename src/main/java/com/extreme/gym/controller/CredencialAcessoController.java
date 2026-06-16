package com.extreme.gym.controller;

import com.extreme.gym.dto.credencial.CredencialAcessoRequestDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoResponseDTO;
import com.extreme.gym.service.CredencialAcessoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequiredArgsConstructor
public class CredencialAcessoController {

    private final CredencialAcessoService credencialAcessoService;

    @GetMapping("/alunos/{alunoId}/credenciais-acesso")
    public ResponseEntity<List<CredencialAcessoResponseDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(credencialAcessoService.listarPorAluno(alunoId));
    }

    @PostMapping("/alunos/{alunoId}/credenciais-acesso")
    public ResponseEntity<CredencialAcessoResponseDTO> criar(
            @PathVariable Long alunoId,
            @Valid @RequestBody CredencialAcessoRequestDTO request
    ) {
        CredencialAcessoResponseDTO credencial = credencialAcessoService.criar(alunoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/credenciais-acesso/{id}")
                .buildAndExpand(credencial.id())
                .toUri();
        return ResponseEntity.created(location).body(credencial);
    }

    @PatchMapping("/credenciais-acesso/{id}/revogar")
    public ResponseEntity<CredencialAcessoResponseDTO> revogar(@PathVariable Long id) {
        return ResponseEntity.ok(credencialAcessoService.revogar(id));
    }
}
