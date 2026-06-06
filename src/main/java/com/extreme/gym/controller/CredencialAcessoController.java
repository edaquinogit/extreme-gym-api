package com.extreme.gym.controller;

import com.extreme.gym.dto.credencial.CredencialAcessoRequestDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoResponseDTO;
import com.extreme.gym.service.CredencialAcessoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CredencialAcessoController {

    private final CredencialAcessoService credencialService;

    @GetMapping("/alunos/{alunoId}/credenciais-acesso")
    public List<CredencialAcessoResponseDTO> listarPorAluno(@PathVariable Long alunoId) {
        return credencialService.listarPorAluno(alunoId);
    }

    @PostMapping("/alunos/{alunoId}/credenciais-acesso")
    @ResponseStatus(HttpStatus.CREATED)
    public CredencialAcessoResponseDTO criar(
            @PathVariable Long alunoId,
            @Valid @RequestBody CredencialAcessoRequestDTO request
    ) {
        return credencialService.criar(alunoId, request);
    }

    @PatchMapping("/credenciais-acesso/{id}/revogar")
    public CredencialAcessoResponseDTO revogar(@PathVariable Long id) {
        return credencialService.revogar(id);
    }
}
