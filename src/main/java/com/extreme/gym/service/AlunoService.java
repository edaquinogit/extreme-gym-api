package com.extreme.gym.service;

import com.extreme.gym.dto.aluno.AlunoRequestDTO;
import com.extreme.gym.dto.aluno.AlunoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoResponseDTO cadastrar(AlunoRequestDTO request) {
        validarEmailDuplicado(request.email());

        Aluno aluno = Aluno.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .build();

        return toResponseDTO(alunoRepository.save(aluno));
    }

    public List<AlunoResponseDTO> listar() {
        return alunoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public AlunoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    public AlunoResponseDTO atualizar(Long id, AlunoRequestDTO request) {
        Aluno aluno = buscarEntidadePorId(id);

        validarEmailDuplicadoAoAtualizar(request.email(), id);

        aluno.setNome(request.nome());
        aluno.setEmail(request.email());
        aluno.setTelefone(request.telefone());

        return toResponseDTO(alunoRepository.save(aluno));
    }

    public void remover(Long id) {
        Aluno aluno = buscarEntidadePorId(id);
        alunoRepository.delete(aluno);
    }

    private void validarEmailDuplicado(String email) {
        if (alunoRepository.existsByEmail(email)) {
            throw new BusinessException("Ja existe aluno cadastrado com este email");
        }
    }

    private void validarEmailDuplicadoAoAtualizar(String email, Long id) {
        if (alunoRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Ja existe aluno cadastrado com este email");
        }
    }

    private Aluno buscarEntidadePorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno nao encontrado com id: " + id));
    }

    private AlunoResponseDTO toResponseDTO(Aluno aluno) {
        return new AlunoResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getTelefone(),
                aluno.getStatus(),
                aluno.getDataCadastro()
        );
    }
}
