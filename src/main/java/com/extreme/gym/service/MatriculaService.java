package com.extreme.gym.service;

import com.extreme.gym.dto.matricula.MatriculaRequestDTO;
import com.extreme.gym.dto.matricula.MatriculaResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final PlanoRepository planoRepository;

    public MatriculaResponseDTO cadastrar(MatriculaRequestDTO request) {
        Aluno aluno = buscarAlunoPorId(request.alunoId());
        Plano plano = buscarPlanoPorId(request.planoId());

        validarPlanoAtivo(plano);
        validarAlunoSemMatriculaAtiva(aluno.getId());

        Matricula matricula = Matricula.builder()
                .aluno(aluno)
                .plano(plano)
                .dataInicio(request.dataInicio())
                .dataFim(request.dataInicio().plusDays(plano.getDuracaoEmDias()))
                .status(StatusMatricula.ATIVA)
                .build();

        return toResponseDTO(matriculaRepository.save(matricula));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listar() {
        return matriculaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MatriculaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    public MatriculaResponseDTO cancelar(Long id) {
        Matricula matricula = buscarEntidadePorId(id);
        matricula.setStatus(StatusMatricula.CANCELADA);

        return toResponseDTO(matriculaRepository.save(matricula));
    }

    private Aluno buscarAlunoPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno nao encontrado com id: " + id));
    }

    private Plano buscarPlanoPorId(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano nao encontrado com id: " + id));
    }

    private Matricula buscarEntidadePorId(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matricula nao encontrada com id: " + id));
    }

    private void validarPlanoAtivo(Plano plano) {
        if (Boolean.FALSE.equals(plano.getAtivo())) {
            throw new BusinessException("Plano inativo nao pode ser usado em matricula");
        }
    }

    private void validarAlunoSemMatriculaAtiva(Long alunoId) {
        if (matriculaRepository.existsByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA)) {
            throw new BusinessException("Aluno ja possui matricula ativa");
        }
    }

    private MatriculaResponseDTO toResponseDTO(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getNome(),
                matricula.getPlano().getId(),
                matricula.getPlano().getNome(),
                matricula.getDataInicio(),
                matricula.getDataFim(),
                matricula.getStatus(),
                matricula.getDataCadastro()
        );
    }
}
