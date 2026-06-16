package com.extreme.gym.controller;

import com.extreme.gym.dto.evento.EventoAcessoLoteRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoLoteResponseDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.service.DeviceApiKeyAuthenticator;
import com.extreme.gym.service.EventoAcessoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventos-acesso")
@RequiredArgsConstructor
public class EventoAcessoController {

    private final EventoAcessoService eventoService;
    private final DeviceApiKeyAuthenticator deviceApiKeyAuthenticator;

    @PostMapping("/sincronizar-lote")
    public EventoAcessoLoteResponseDTO sincronizarLote(
            @RequestHeader(value = "X-Device-Id", required = false) Long dispositivoId,
            @RequestHeader(value = "X-Gateway-Id", required = false) Long gatewayId,
            @RequestHeader(value = "X-Device-Api-Key", required = false) String apiKey,
            @Valid @RequestBody EventoAcessoLoteRequestDTO request
    ) {
        DispositivoAcesso dispositivo = deviceApiKeyAuthenticator.authenticate(resolveDeviceId(dispositivoId, gatewayId), apiKey);
        return eventoService.sincronizarLote(dispositivo, request);
    }

    @GetMapping
    public List<EventoAcessoResponseDTO> listar() {
        return eventoService.listar();
    }

    private Long resolveDeviceId(Long dispositivoId, Long gatewayId) {
        return dispositivoId != null ? dispositivoId : gatewayId;
    }
}
