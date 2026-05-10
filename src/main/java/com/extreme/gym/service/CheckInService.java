package com.extreme.gym.service;

import com.extreme.gym.dto.checkin.CheckInRequestDTO;
import com.extreme.gym.dto.checkin.CheckInResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInService {

    private static final String MOTIVO_PERMITIDO = "Check-in permitido";

    private final CheckInRepository checkInRepository;
    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagamentoRepository pagamentoRepository;

    public CheckInResponseDTO registrar(CheckInRequestDTO request) {
        Aluno aluno = buscarAlunoPorId(request.alunoId());
        ResultadoValidacao resultado = validarAcesso(aluno);

        CheckIn checkIn = CheckIn.builder()
                .aluno(aluno)
                .matricula(resultado.matricula())
                .dataHora(LocalDateTime.now())
                .permitido(resultado.permitido())
                .motivo(resultado.motivo())
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

    private Aluno buscarAlunoPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno nao encontrado com id: " + id));
    }

    private CheckIn buscarEntidadePorId(Long id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in nao encontrado com id: " + id));
    }

    private ResultadoValidacao validarAcesso(Aluno aluno) {
        if (aluno.getStatus() == StatusAluno.BLOQUEADO) {
            return ResultadoValidacao.bloqueado("Aluno bloqueado");
        }
        if (aluno.getStatus() == StatusAluno.CANCELADO) {
            return ResultadoValidacao.bloqueado("Aluno cancelado");
        }
        if (aluno.getStatus() == StatusAluno.INADIMPLENTE) {
            return ResultadoValidacao.bloqueado("Aluno inadimplente");
        }

        return matriculaRepository.findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA)
                .map(this::validarMatricula)
                .orElseGet(() -> ResultadoValidacao.bloqueado("Aluno nao possui matricula ativa"));
    }

    private ResultadoValidacao validarMatricula(Matricula matricula) {
        if (matricula.getDataFim().isBefore(LocalDate.now())) {
            return ResultadoValidacao.bloqueado("Matricula vencida");
        }
        if (!pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO)) {
            return ResultadoValidacao.bloqueado("Matricula nao possui pagamento pago");
        }

        return ResultadoValidacao.permitido(matricula);
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

    private record ResultadoValidacao(Boolean permitido, String motivo, Matricula matricula) {

        private static ResultadoValidacao permitido(Matricula matricula) {
            return new ResultadoValidacao(true, MOTIVO_PERMITIDO, matricula);
        }

        private static ResultadoValidacao bloqueado(String motivo) {
            return new ResultadoValidacao(false, motivo, null);
        }
    }
}
