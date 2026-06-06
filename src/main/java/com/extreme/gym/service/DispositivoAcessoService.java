package com.extreme.gym.service;

import com.extreme.gym.dto.dispositivo.DispositivoAcessoCreatedResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatResponseDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.DispositivoAcessoRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DispositivoAcessoService {

    private static final int API_KEY_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DispositivoAcessoRepository dispositivoRepository;
    private final PasswordEncoder passwordEncoder;

    public DispositivoAcessoCreatedResponseDTO criar(DispositivoAcessoRequestDTO request) {
        if (request.identificadorExterno() != null
                && !request.identificadorExterno().isBlank()
                && dispositivoRepository.findByIdentificadorExterno(request.identificadorExterno()).isPresent()) {
            throw new BusinessException("Dispositivo ja cadastrado com este identificador externo");
        }

        String apiKey = gerarApiKey();
        DispositivoAcesso dispositivo = DispositivoAcesso.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .status(StatusDispositivoAcesso.ATIVO)
                .modoOperacao(request.modoOperacao())
                .identificadorExterno(blankToNull(request.identificadorExterno()))
                .apiKeyHash(passwordEncoder.encode(apiKey))
                .fabricante(blankToNull(request.fabricante()))
                .modelo(blankToNull(request.modelo()))
                .ipLocal(blankToNull(request.ipLocal()))
                .unidade(blankToNull(request.unidade()))
                .build();

        DispositivoAcesso salvo = dispositivoRepository.save(dispositivo);
        return new DispositivoAcessoCreatedResponseDTO(toResponseDTO(salvo), apiKey);
    }

    @Transactional(readOnly = true)
    public List<DispositivoAcessoResponseDTO> listar() {
        return dispositivoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DispositivoAcesso buscarEntidade(Long id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo nao encontrado com id: " + id));
    }

    public DispositivoAcessoCreatedResponseDTO rotacionarApiKey(Long id) {
        DispositivoAcesso dispositivo = buscarEntidade(id);
        String apiKey = gerarApiKey();
        dispositivo.setApiKeyHash(passwordEncoder.encode(apiKey));
        DispositivoAcesso salvo = dispositivoRepository.save(dispositivo);
        return new DispositivoAcessoCreatedResponseDTO(toResponseDTO(salvo), apiKey);
    }

    public DispositivoHeartbeatResponseDTO registrarHeartbeat(
            DispositivoAcesso dispositivo,
            DispositivoHeartbeatRequestDTO request
    ) {
        LocalDateTime now = LocalDateTime.now();
        dispositivo.setUltimaComunicacaoEm(now);
        if (request.modoOperacao() != null) {
            dispositivo.setModoOperacao(request.modoOperacao());
        }
        if (request.status() == StatusDispositivoAcesso.OFFLINE) {
            dispositivo.setStatus(StatusDispositivoAcesso.OFFLINE);
        } else if (dispositivo.getStatus() == StatusDispositivoAcesso.OFFLINE) {
            dispositivo.setStatus(StatusDispositivoAcesso.ATIVO);
        }

        DispositivoAcesso salvo = dispositivoRepository.save(dispositivo);
        return new DispositivoHeartbeatResponseDTO(salvo.getId(), salvo.getStatus(), salvo.getUltimaComunicacaoEm(), true);
    }

    public DispositivoAcessoResponseDTO toResponseDTO(DispositivoAcesso dispositivo) {
        return new DispositivoAcessoResponseDTO(
                dispositivo.getId(),
                dispositivo.getNome(),
                dispositivo.getTipo(),
                dispositivo.getStatus(),
                dispositivo.getModoOperacao(),
                dispositivo.getIdentificadorExterno(),
                dispositivo.getFabricante(),
                dispositivo.getModelo(),
                dispositivo.getIpLocal(),
                dispositivo.getUnidade(),
                dispositivo.getUltimaComunicacaoEm(),
                dispositivo.getCriadoEm(),
                dispositivo.getAtualizadoEm()
        );
    }

    private String gerarApiKey() {
        byte[] bytes = new byte[API_KEY_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
