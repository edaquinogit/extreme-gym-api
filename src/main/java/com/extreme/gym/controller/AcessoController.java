package com.extreme.gym.controller;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinRequestDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinResponseDTO;
import com.extreme.gym.dto.acesso.FichaAcessoResponseDTO;
import com.extreme.gym.dto.acesso.LiberacaoManualRequestDTO;
import com.extreme.gym.service.AcessoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/acessos")
@RequiredArgsConstructor
public class AcessoController {

    private final AcessoService acessoService;

    @PostMapping("/validar")
    public ResponseEntity<AcessoResponseDTO> validar(@Valid @RequestBody AcessoRequestDTO request) {
        return ResponseEntity.ok(acessoService.validar(request));
    }

    @PostMapping("/assistido/validar-pin")
    public ResponseEntity<AssistenciaPinResponseDTO> validarPinAssistido(
            @Valid @RequestBody AssistenciaPinRequestDTO request
    ) {
        return ResponseEntity.ok(acessoService.validarPinAssistido(request));
    }

    @PostMapping("/liberacao-manual")
    public ResponseEntity<AcessoResponseDTO> liberarManual(@Valid @RequestBody LiberacaoManualRequestDTO request) {
        return ResponseEntity.ok(acessoService.liberarManual(request));
    }

    @GetMapping("/alunos/{alunoId}/ficha")
    public ResponseEntity<FichaAcessoResponseDTO> buscarFichaAcesso(@PathVariable Long alunoId) {
        return ResponseEntity.ok(acessoService.buscarFichaAcesso(alunoId));
    }
}
