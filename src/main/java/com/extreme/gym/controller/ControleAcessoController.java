package com.extreme.gym.controller;

import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.service.ControleAcessoService;
import com.extreme.gym.service.DeviceApiKeyAuthenticator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final DeviceApiKeyAuthenticator deviceApiKeyAuthenticator;

    @GetMapping("/snapshot-autorizados")
    public SnapshotAutorizadosResponseDTO snapshotAutorizados(
            @RequestHeader(value = "X-Device-Id", required = false) Long dispositivoId,
            @RequestHeader(value = "X-Gateway-Id", required = false) Long gatewayId,
            @RequestHeader(value = "X-Device-Api-Key", required = false) String apiKey
    ) {
        DispositivoAcesso dispositivo = deviceApiKeyAuthenticator.authenticate(resolveDeviceId(dispositivoId, gatewayId), apiKey);
        return controleAcessoService.gerarSnapshot(dispositivo);
    }

    @PostMapping("/validar-dispositivo")
    public ValidarDispositivoResponseDTO validarDispositivo(
            @RequestHeader(value = "X-Device-Id", required = false) Long dispositivoId,
            @RequestHeader(value = "X-Gateway-Id", required = false) Long gatewayId,
            @RequestHeader(value = "X-Device-Api-Key", required = false) String apiKey,
            @Valid @RequestBody ValidarDispositivoRequestDTO request
    ) {
        DispositivoAcesso dispositivo = deviceApiKeyAuthenticator.authenticate(resolveDeviceId(dispositivoId, gatewayId), apiKey);
        return controleAcessoService.validarDispositivo(dispositivo, request);
    }

    private Long resolveDeviceId(Long dispositivoId, Long gatewayId) {
        return dispositivoId != null ? dispositivoId : gatewayId;
    }
}
