package com.extreme.gym.service;

import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.repository.DispositivoAcessoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceApiKeyAuthenticator {

    private final DispositivoAcessoRepository dispositivoRepository;
    private final PasswordEncoder passwordEncoder;

    public DispositivoAcesso authenticate(Long dispositivoId, String apiKey) {
        if (dispositivoId == null) {
            throw new BadCredentialsException("ID do dispositivo ausente");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadCredentialsException("API key do dispositivo ausente");
        }

        DispositivoAcesso dispositivo = dispositivoRepository.findById(dispositivoId)
                .orElseThrow(() -> new BadCredentialsException("Dispositivo nao encontrado"));

        if (!passwordEncoder.matches(apiKey, dispositivo.getApiKeyHash())) {
            throw new BadCredentialsException("API key do dispositivo invalida");
        }
        if (dispositivo.getStatus() == StatusDispositivoAcesso.INATIVO) {
            throw new AccessDeniedException("Dispositivo inativo");
        }

        return dispositivo;
    }

    public void ensureAutomaticAccessAllowed(DispositivoAcesso dispositivo) {
        if (dispositivo.getStatus() == StatusDispositivoAcesso.MANUTENCAO) {
            throw new AccessDeniedException("Dispositivo em manutencao");
        }
    }
}
