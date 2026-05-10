package com.extreme.gym.service;

import com.extreme.gym.dto.plano.PlanoRequestDTO;
import com.extreme.gym.dto.plano.PlanoResponseDTO;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.PlanoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;

    public PlanoResponseDTO cadastrar(PlanoRequestDTO request) {
        validarNomeDuplicado(request.nome());

        Plano plano = Plano.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valorMensal(request.valorMensal())
                .duracaoEmDias(request.duracaoEmDias())
                .build();

        return toResponseDTO(planoRepository.save(plano));
    }

    public List<PlanoResponseDTO> listar() {
        return planoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PlanoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    public PlanoResponseDTO atualizar(Long id, PlanoRequestDTO request) {
        Plano plano = buscarEntidadePorId(id);

        validarNomeDuplicadoAoAtualizar(request.nome(), id);

        plano.setNome(request.nome());
        plano.setDescricao(request.descricao());
        plano.setValorMensal(request.valorMensal());
        plano.setDuracaoEmDias(request.duracaoEmDias());

        return toResponseDTO(planoRepository.save(plano));
    }

    public void remover(Long id) {
        Plano plano = buscarEntidadePorId(id);
        plano.setAtivo(false);
        planoRepository.save(plano);
    }

    private void validarNomeDuplicado(String nome) {
        if (planoRepository.existsByNome(nome)) {
            throw new BusinessException("Ja existe plano cadastrado com este nome");
        }
    }

    private void validarNomeDuplicadoAoAtualizar(String nome, Long id) {
        if (planoRepository.existsByNomeAndIdNot(nome, id)) {
            throw new BusinessException("Ja existe plano cadastrado com este nome");
        }
    }

    private Plano buscarEntidadePorId(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano nao encontrado com id: " + id));
    }

    private PlanoResponseDTO toResponseDTO(Plano plano) {
        return new PlanoResponseDTO(
                plano.getId(),
                plano.getNome(),
                plano.getDescricao(),
                plano.getValorMensal(),
                plano.getDuracaoEmDias(),
                plano.getAtivo(),
                plano.getDataCadastro()
        );
    }
}
