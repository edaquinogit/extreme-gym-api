package com.extreme.gym.service;

import com.extreme.gym.dto.checkin.CheckInRequestDTO;
import com.extreme.gym.dto.checkin.CheckInResponseDTO;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.CheckInRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInService {

    private static final String MOTIVO_CHECK_IN_PERMITIDO = "Check-in permitido";

    private final CheckInRepository checkInRepository;
    private final AcessoService acessoService;

    public CheckInResponseDTO registrar(CheckInRequestDTO request) {
        AcessoService.ResultadoAcesso resultado = acessoService.validarAluno(request.alunoId());

        CheckIn checkIn = CheckIn.builder()
                .aluno(resultado.aluno())
                .matricula(Boolean.TRUE.equals(resultado.acessoLiberado()) ? resultado.matricula() : null)
                .dataHora(LocalDateTime.now())
                .permitido(resultado.acessoLiberado())
                .motivo(montarMotivoCheckIn(resultado))
                .build();

        return toResponseDTO(checkInRepository.save(checkIn));
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> listar() {
        return checkInRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> listarPorAluno(Long alunoId) {
        return checkInRepository.findByAlunoId(alunoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CheckInResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    private CheckIn buscarEntidadePorId(Long id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in nao encontrado com id: " + id));
    }

    private CheckInResponseDTO toResponseDTO(CheckIn checkIn) {
        Matricula matricula = checkIn.getMatricula();

        return new CheckInResponseDTO(
                checkIn.getId(),
                checkIn.getAluno().getId(),
                checkIn.getAluno().getNome(),
                matricula != null ? matricula.getId() : null,
                checkIn.getPermitido(),
                checkIn.getMotivo(),
                checkIn.getDataHora()
        );
    }

    private String montarMotivoCheckIn(AcessoService.ResultadoAcesso resultado) {
        if (Boolean.TRUE.equals(resultado.acessoLiberado())) {
            return MOTIVO_CHECK_IN_PERMITIDO;
        }

        return resultado.motivo();
    }
}
