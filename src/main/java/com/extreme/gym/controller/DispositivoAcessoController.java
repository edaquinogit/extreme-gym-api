package com.extreme.gym.controller;

import com.extreme.gym.dto.dispositivo.DispositivoAcessoCreatedResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatResponseDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.service.DeviceApiKeyAuthenticator;
import com.extreme.gym.service.DispositivoAcessoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dispositivos-acesso")
@RequiredArgsConstructor
public class DispositivoAcessoController {

    private final DispositivoAcessoService dispositivoService;
    private final DeviceApiKeyAuthenticator deviceApiKeyAuthenticator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DispositivoAcessoCreatedResponseDTO criar(@Valid @RequestBody DispositivoAcessoRequestDTO request) {
        return dispositivoService.criar(request);
    }

    @GetMapping
    public List<DispositivoAcessoResponseDTO> listar() {
        return dispositivoService.listar();
    }

    @PostMapping("/{id}/rotate-api-key")
    public DispositivoAcessoCreatedResponseDTO rotacionarApiKey(@PathVariable Long id) {
        return dispositivoService.rotacionarApiKey(id);
    }

    @PostMapping("/{id}/heartbeat")
    public DispositivoHeartbeatResponseDTO heartbeat(
            @PathVariable Long id,
            @RequestHeader(value = "X-Device-Api-Key", required = false) String apiKey,
            @Valid @RequestBody DispositivoHeartbeatRequestDTO request
    ) {
        DispositivoAcesso dispositivo = deviceApiKeyAuthenticator.authenticate(id, apiKey);
        return dispositivoService.registrarHeartbeat(dispositivo, request);
    }
}
