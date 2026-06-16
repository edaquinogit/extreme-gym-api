package com.extreme.gym.controller;

import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoStatusUpdateDTO;
import com.extreme.gym.service.DispositivoAcessoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/dispositivos-acesso")
@RequiredArgsConstructor
public class DispositivoAcessoController {

    private final DispositivoAcessoService dispositivoAcessoService;

    @GetMapping
    public ResponseEntity<List<DispositivoAcessoResponseDTO>> listar() {
        return ResponseEntity.ok(dispositivoAcessoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispositivoAcessoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(dispositivoAcessoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DispositivoAcessoResponseDTO> criar(@Valid @RequestBody DispositivoAcessoRequestDTO request) {
        DispositivoAcessoResponseDTO dispositivo = dispositivoAcessoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dispositivo.id())
                .toUri();
        return ResponseEntity.created(location).body(dispositivo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DispositivoAcessoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DispositivoAcessoRequestDTO request
    ) {
        return ResponseEntity.ok(dispositivoAcessoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DispositivoAcessoResponseDTO> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody DispositivoStatusUpdateDTO request
    ) {
        return ResponseEntity.ok(dispositivoAcessoService.alterarStatus(id, request));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<DispositivoAcessoResponseDTO> heartbeat(
            @PathVariable Long id,
            @RequestBody(required = false) DispositivoHeartbeatRequestDTO request
    ) {
        DispositivoHeartbeatRequestDTO safeRequest = request == null ? new DispositivoHeartbeatRequestDTO(null) : request;
        return ResponseEntity.ok(dispositivoAcessoService.heartbeat(id, safeRequest));
    }
}
