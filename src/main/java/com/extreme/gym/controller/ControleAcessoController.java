package com.extreme.gym.controller;

import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.service.ControleAcessoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/controle-acesso")
@RequiredArgsConstructor
public class ControleAcessoController {

    private final ControleAcessoService controleAcessoService;

    @PostMapping("/validar-dispositivo")
    public ResponseEntity<ValidarDispositivoResponseDTO> validarDispositivo(
            @Valid @RequestBody ValidarDispositivoRequestDTO request,
            @RequestHeader(value = "X-Device-Api-Key", required = false) String apiKey
    ) {
        return ResponseEntity.ok(controleAcessoService.validarDispositivo(request, apiKey));
    }

    @GetMapping("/snapshot-autorizados")
    public ResponseEntity<SnapshotAutorizadosResponseDTO> snapshotAutorizados() {
        return ResponseEntity.ok(controleAcessoService.gerarSnapshot(null));
    }

    @GetMapping("/snapshot-autorizados/{dispositivoId}")
    public ResponseEntity<SnapshotAutorizadosResponseDTO> snapshotAutorizadosPorDispositivo(
            @PathVariable Long dispositivoId
    ) {
        return ResponseEntity.ok(controleAcessoService.gerarSnapshot(dispositivoId));
    }
}
