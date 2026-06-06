package com.extreme.gym.service;

import com.extreme.gym.dto.credencial.CredencialAcessoRequestDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoResponseDTO;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CredencialAcessoService {

    private final CredencialAcessoRepository credencialRepository;
    private final AlunoRepository alunoRepository;

    public CredencialAcessoResponseDTO criar(Long alunoId, CredencialAcessoRequestDTO request) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new ResourceNotFoundException("Aluno nao encontrado com id: " + alunoId);
        }
        if (credencialRepository.existsByTipoAndIdentificadorExterno(request.tipo(), request.identificadorExterno())) {
            throw new BusinessException("Credencial ja cadastrada com este tipo e identificador");
        }
        validarReferenciaSegura(request.identificadorExterno());

        CredencialAcesso credencial = CredencialAcesso.builder()
                .alunoId(alunoId)
                .tipo(request.tipo())
                .identificadorExterno(request.identificadorExterno())
                .fornecedor(blankToNull(request.fornecedor()))
                .status(StatusCredencialAcesso.ATIVA)
                .termoAceitoEm(request.termoAceitoEm())
                .versaoTermo(blankToNull(request.versaoTermo()))
                .build();

        return toResponseDTO(credencialRepository.save(credencial));
    }

    @Transactional(readOnly = true)
    public List<CredencialAcessoResponseDTO> listarPorAluno(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new ResourceNotFoundException("Aluno nao encontrado com id: " + alunoId);
        }
        return credencialRepository.findByAlunoId(alunoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CredencialAcessoResponseDTO revogar(Long id) {
        CredencialAcesso credencial = credencialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial nao encontrada com id: " + id));
        credencial.setStatus(StatusCredencialAcesso.REVOGADA);
        credencial.setRevogadoEm(LocalDateTime.now());
        return toResponseDTO(credencialRepository.save(credencial));
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

    private void validarReferenciaSegura(String identificadorExterno) {
        String normalized = identificadorExterno.toLowerCase();
        if (normalized.startsWith("data:image") || normalized.length() > 512) {
            throw new BusinessException("Credencial deve armazenar apenas referencia externa segura");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
