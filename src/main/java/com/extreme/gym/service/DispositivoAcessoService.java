package com.extreme.gym.service;

import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoStatusUpdateDTO;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.DispositivoAcessoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DispositivoAcessoService {

    private static final Logger log = LoggerFactory.getLogger(DispositivoAcessoService.class);

    private final DispositivoAcessoRepository dispositivoAcessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final AuditoriaAcessoService auditoriaAcessoService;

    @Transactional
    public DispositivoAcessoResponseDTO criar(DispositivoAcessoRequestDTO request) {
        validarNomeDuplicado(request.nome());
        DispositivoAcesso dispositivo = DispositivoAcesso.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .fabricante(request.fabricante())
                .modelo(request.modelo())
                .identificadorExterno(request.identificadorExterno())
                .ipLocal(request.ipLocal())
                .unidade(request.unidade())
                .status(request.status())
                .modoOperacao(request.modoOperacao())
                .apiKeyHash(hashApiKey(request.apiKey()))
                .build();

        DispositivoAcesso salvo = dispositivoAcessoRepository.save(dispositivo);
        auditoriaAcessoService.registrar("CRIAR_DISPOSITIVO", "DispositivoAcesso", salvo.getId(), salvo.getId(),
                "SISTEMA", "Dispositivo de acesso criado");
        log.info("Dispositivo de acesso criado: id={}, tipo={}, status={}", salvo.getId(), salvo.getTipo(), salvo.getStatus());
        return toResponseDTO(salvo);
    }

    public List<DispositivoAcessoResponseDTO> listar() {
        return dispositivoAcessoRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public DispositivoAcessoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public DispositivoAcessoResponseDTO atualizar(Long id, DispositivoAcessoRequestDTO request) {
        DispositivoAcesso dispositivo = buscarEntidadePorId(id);
        validarNomeDuplicadoAoAtualizar(request.nome(), id);

        dispositivo.setNome(request.nome());
        dispositivo.setTipo(request.tipo());
        dispositivo.setFabricante(request.fabricante());
        dispositivo.setModelo(request.modelo());
        dispositivo.setIdentificadorExterno(request.identificadorExterno());
        dispositivo.setIpLocal(request.ipLocal());
        dispositivo.setUnidade(request.unidade());
        dispositivo.setStatus(request.status());
        dispositivo.setModoOperacao(request.modoOperacao());
        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            dispositivo.setApiKeyHash(hashApiKey(request.apiKey()));
        }

        DispositivoAcesso salvo = dispositivoAcessoRepository.save(dispositivo);
        auditoriaAcessoService.registrar("ALTERAR_DISPOSITIVO", "DispositivoAcesso", salvo.getId(), salvo.getId(),
                "SISTEMA", "Dispositivo de acesso alterado");
        log.info("Dispositivo de acesso alterado: id={}", salvo.getId());
        return toResponseDTO(salvo);
    }

    @Transactional
    public DispositivoAcessoResponseDTO alterarStatus(Long id, DispositivoStatusUpdateDTO request) {
        DispositivoAcesso dispositivo = buscarEntidadePorId(id);
        dispositivo.setStatus(request.status());
        DispositivoAcesso salvo = dispositivoAcessoRepository.save(dispositivo);
        auditoriaAcessoService.registrar("ALTERAR_STATUS_DISPOSITIVO", "DispositivoAcesso", salvo.getId(), salvo.getId(),
                "SISTEMA", "Status do dispositivo alterado para " + request.status());
        log.info("Status de dispositivo alterado: id={}, status={}", salvo.getId(), salvo.getStatus());
        return toResponseDTO(salvo);
    }

    @Transactional
    public DispositivoAcessoResponseDTO heartbeat(Long id, DispositivoHeartbeatRequestDTO request) {
        DispositivoAcesso dispositivo = buscarEntidadePorId(id);
        dispositivo.setUltimaComunicacaoEm(LocalDateTime.now(clock));
        DispositivoAcesso salvo = dispositivoAcessoRepository.save(dispositivo);
        auditoriaAcessoService.registrar("HEARTBEAT_DISPOSITIVO", "DispositivoAcesso", salvo.getId(), salvo.getId(),
                "GATEWAY", "Heartbeat recebido");
        log.info("Heartbeat recebido: dispositivoId={}", salvo.getId());
        return toResponseDTO(salvo);
    }

    public DispositivoAcesso buscarEntidadePorId(Long id) {
        return dispositivoAcessoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo de acesso nao encontrado com id: " + id));
    }

    public void validarOperacional(DispositivoAcesso dispositivo) {
        if (dispositivo.getStatus() != StatusDispositivoAcesso.ATIVO) {
            throw new BusinessException("Dispositivo de acesso nao esta operacional");
        }
    }

    public void validarApiKey(DispositivoAcesso dispositivo, String apiKey, boolean obrigatoria) {
        if (!obrigatoria) {
            return;
        }
        if (dispositivo.getApiKeyHash() == null || dispositivo.getApiKeyHash().isBlank()) {
            throw new BusinessException("Dispositivo sem credencial tecnica configurada");
        }
        if (apiKey == null || apiKey.isBlank() || !passwordEncoder.matches(apiKey, dispositivo.getApiKeyHash())) {
            auditoriaAcessoService.registrar("FALHA_AUTENTICACAO_TECNICA", "DispositivoAcesso", dispositivo.getId(),
                    dispositivo.getId(), "GATEWAY", "API key invalida ou ausente");
            throw new BusinessException("Credencial tecnica invalida");
        }
    }

    public DispositivoAcessoResponseDTO toResponseDTO(DispositivoAcesso dispositivo) {
        return new DispositivoAcessoResponseDTO(
                dispositivo.getId(),
                dispositivo.getNome(),
                dispositivo.getTipo(),
                dispositivo.getFabricante(),
                dispositivo.getModelo(),
                dispositivo.getIdentificadorExterno(),
                dispositivo.getIpLocal(),
                dispositivo.getUnidade(),
                dispositivo.getStatus(),
                dispositivo.getModoOperacao(),
                dispositivo.getUltimaComunicacaoEm(),
                dispositivo.getCriadoEm(),
                dispositivo.getAtualizadoEm()
        );
    }

    private void validarNomeDuplicado(String nome) {
        if (dispositivoAcessoRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException("Ja existe dispositivo de acesso com este nome");
        }
    }

    private void validarNomeDuplicadoAoAtualizar(String nome, Long id) {
        if (dispositivoAcessoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException("Ja existe dispositivo de acesso com este nome");
        }
    }

    private String hashApiKey(String apiKey) {
        return apiKey == null || apiKey.isBlank() ? null : passwordEncoder.encode(apiKey);
    }
}
