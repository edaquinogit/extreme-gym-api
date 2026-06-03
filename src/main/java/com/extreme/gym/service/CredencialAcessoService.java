package com.extreme.gym.service;

import com.extreme.gym.dto.credencial.CredencialAcessoRequestDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoResponseDTO;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CredencialAcessoService {

    private static final Logger log = LoggerFactory.getLogger(CredencialAcessoService.class);

    private final CredencialAcessoRepository credencialAcessoRepository;
    private final AlunoRepository alunoRepository;
    private final AuditoriaAcessoService auditoriaAcessoService;
    private final Clock clock;

    public List<CredencialAcessoResponseDTO> listarPorAluno(Long alunoId) {
        validarAlunoExiste(alunoId);
        return credencialAcessoRepository.findByAlunoIdOrderByCriadoEmDesc(alunoId)
                .stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public CredencialAcessoResponseDTO criar(Long alunoId, CredencialAcessoRequestDTO request) {
        validarAlunoExiste(alunoId);
        CredencialAcesso credencial = CredencialAcesso.builder()
                .alunoId(alunoId)
                .tipo(request.tipo())
                .identificadorExterno(request.identificadorExterno())
                .fornecedor(request.fornecedor())
                .status(request.status() == null ? StatusCredencialAcesso.PENDENTE : request.status())
                .termoAceitoEm(request.termoAceitoEm())
                .versaoTermo(request.versaoTermo())
                .build();

        CredencialAcesso salva = credencialAcessoRepository.save(credencial);
        auditoriaAcessoService.registrar("CRIAR_CREDENCIAL_ACESSO", "CredencialAcesso", salva.getId(), null,
                "SISTEMA", "Credencial de acesso criada sem biometria bruta");
        log.info("Credencial de acesso criada: id={}, alunoId={}, tipo={}", salva.getId(), alunoId, salva.getTipo());
        return toResponseDTO(salva);
    }

    @Transactional
    public CredencialAcessoResponseDTO revogar(Long id) {
        CredencialAcesso credencial = credencialAcessoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial de acesso nao encontrada com id: " + id));
        credencial.setStatus(StatusCredencialAcesso.REVOGADA);
        credencial.setRevogadoEm(LocalDateTime.now(clock));
        CredencialAcesso salva = credencialAcessoRepository.save(credencial);
        auditoriaAcessoService.registrar("REVOGAR_CREDENCIAL_ACESSO", "CredencialAcesso", salva.getId(), null,
                "SISTEMA", "Credencial de acesso revogada");
        log.info("Credencial de acesso revogada: id={}, alunoId={}", salva.getId(), salva.getAlunoId());
        return toResponseDTO(salva);
    }

    public CredencialAcessoResponseDTO toResponseDTO(CredencialAcesso credencial) {
        return new CredencialAcessoResponseDTO(
                credencial.getId(),
                credencial.getAlunoId(),
                credencial.getTipo(),
                credencial.getIdentificadorExterno(),
                credencial.getFornecedor(),
                credencial.getStatus(),
                credencial.getCadastradoEm(),
                credencial.getRevogadoEm(),
                credencial.getTermoAceitoEm(),
                credencial.getVersaoTermo(),
                credencial.getCriadoEm(),
                credencial.getAtualizadoEm()
        );
    }

    private void validarAlunoExiste(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new ResourceNotFoundException("Aluno nao encontrado com id: " + alunoId);
        }
    }
}
