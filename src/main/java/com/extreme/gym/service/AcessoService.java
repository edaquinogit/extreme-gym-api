package com.extreme.gym.service;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcessoService {

    private static final String MOTIVO_LIBERADO = "Acesso liberado";

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagamentoRepository pagamentoRepository;

    public AcessoResponseDTO validar(AcessoRequestDTO request) {
        return toResponseDTO(validarAluno(request.alunoId()));
    }

    public ResultadoAcesso validarAluno(Long alunoId) {
        Aluno aluno = buscarAlunoPorId(alunoId);

        if (aluno.getStatus() == StatusAluno.BLOQUEADO) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno bloqueado");
        }
        if (aluno.getStatus() == StatusAluno.CANCELADO) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno cancelado");
        }
        if (aluno.getStatus() == StatusAluno.INADIMPLENTE) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno inadimplente");
        }

        return matriculaRepository.findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA)
                .map(matricula -> validarMatricula(aluno, matricula))
                .orElseGet(() -> ResultadoAcesso.bloqueado(aluno, "Aluno nao possui matricula ativa"));
    }

    private Aluno buscarAlunoPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno nao encontrado com id: " + id));
    }

    private ResultadoAcesso validarMatricula(Aluno aluno, Matricula matricula) {
        if (matricula.getDataFim().isBefore(LocalDate.now())) {
            return ResultadoAcesso.bloqueado(aluno, matricula, "Matricula vencida");
        }
        if (!pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO)) {
            return ResultadoAcesso.bloqueado(aluno, matricula, "Matricula nao possui pagamento pago");
        }

        return ResultadoAcesso.liberado(aluno, matricula);
    }

    private AcessoResponseDTO toResponseDTO(ResultadoAcesso resultado) {
        Matricula matricula = resultado.matricula();

        return new AcessoResponseDTO(
                resultado.aluno().getId(),
                resultado.aluno().getNome(),
                resultado.acessoLiberado(),
                resultado.motivo(),
                matricula != null ? matricula.getId() : null,
                matricula != null ? matricula.getDataFim() : null
        );
    }

    public record ResultadoAcesso(
            Aluno aluno,
            Matricula matricula,
            Boolean acessoLiberado,
            String motivo
    ) {

        private static ResultadoAcesso liberado(Aluno aluno, Matricula matricula) {
            return new ResultadoAcesso(aluno, matricula, true, MOTIVO_LIBERADO);
        }

        private static ResultadoAcesso bloqueado(Aluno aluno, String motivo) {
            return bloqueado(aluno, null, motivo);
        }

        private static ResultadoAcesso bloqueado(Aluno aluno, Matricula matricula, String motivo) {
            return new ResultadoAcesso(aluno, matricula, false, motivo);
        }
    }
}
