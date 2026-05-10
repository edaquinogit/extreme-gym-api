package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.plano.PlanoRequestDTO;
import com.extreme.gym.dto.plano.PlanoResponseDTO;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.PlanoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @Mock
    private PlanoRepository planoRepository;

    @InjectMocks
    private PlanoService planoService;

    @Test
    void deveCadastrarPlanoComSucesso() {
        PlanoRequestDTO request = criarRequest("Plano Mensal", "Acesso livre por 30 dias");
        LocalDateTime dataCadastro = LocalDateTime.now();

        when(planoRepository.existsByNome(request.nome())).thenReturn(false);
        when(planoRepository.save(any(Plano.class))).thenAnswer(invocation -> {
            Plano plano = invocation.getArgument(0);
            plano.setId(1L);
            plano.setAtivo(true);
            plano.setDataCadastro(dataCadastro);
            return plano;
        });

        PlanoResponseDTO response = planoService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo(request.nome());
        assertThat(response.descricao()).isEqualTo(request.descricao());
        assertThat(response.valorMensal()).isEqualByComparingTo(request.valorMensal());
        assertThat(response.duracaoEmDias()).isEqualTo(request.duracaoEmDias());
        assertThat(response.ativo()).isTrue();
        assertThat(response.dataCadastro()).isEqualTo(dataCadastro);
        verify(planoRepository).save(any(Plano.class));
    }

    @Test
    void naoDeveCadastrarPlanoComNomeDuplicado() {
        PlanoRequestDTO request = criarRequest("Plano Mensal", "Acesso livre por 30 dias");

        when(planoRepository.existsByNome(request.nome())).thenReturn(true);

        assertThatThrownBy(() -> planoService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe plano cadastrado com este nome");

        verify(planoRepository, never()).save(any(Plano.class));
    }

    @Test
    void deveListarPlanos() {
        Plano mensal = criarPlano(1L, "Plano Mensal", "Acesso livre por 30 dias");
        Plano anual = criarPlano(2L, "Plano Anual", "Acesso livre por 365 dias");

        when(planoRepository.findAll()).thenReturn(List.of(mensal, anual));

        List<PlanoResponseDTO> response = planoService.listar();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(PlanoResponseDTO::nome)
                .containsExactly("Plano Mensal", "Plano Anual");
    }

    @Test
    void deveBuscarPlanoPorId() {
        Long planoId = 1L;
        Plano plano = criarPlano(planoId, "Plano Mensal", "Acesso livre por 30 dias");

        when(planoRepository.findById(planoId)).thenReturn(Optional.of(plano));

        PlanoResponseDTO response = planoService.buscarPorId(planoId);

        assertThat(response.id()).isEqualTo(planoId);
        assertThat(response.nome()).isEqualTo(plano.getNome());
        assertThat(response.descricao()).isEqualTo(plano.getDescricao());
        assertThat(response.valorMensal()).isEqualByComparingTo(plano.getValorMensal());
        assertThat(response.duracaoEmDias()).isEqualTo(plano.getDuracaoEmDias());
        assertThat(response.ativo()).isTrue();
        assertThat(response.dataCadastro()).isEqualTo(plano.getDataCadastro());
    }

    @Test
    void deveLancarErroAoBuscarPlanoInexistente() {
        Long planoId = 99L;

        when(planoRepository.findById(planoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planoService.buscarPorId(planoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plano nao encontrado com id: 99");
    }

    @Test
    void deveAtualizarPlanoComSucessoMantendoOProprioNome() {
        Long planoId = 1L;
        Plano plano = criarPlano(planoId, "Plano Mensal", "Acesso livre por 30 dias");
        PlanoRequestDTO request = criarRequest("Plano Mensal", "Acesso completo por 30 dias");

        when(planoRepository.findById(planoId)).thenReturn(Optional.of(plano));
        when(planoRepository.existsByNomeAndIdNot(request.nome(), planoId)).thenReturn(false);
        when(planoRepository.save(plano)).thenReturn(plano);

        PlanoResponseDTO response = planoService.atualizar(planoId, request);

        assertThat(response.id()).isEqualTo(planoId);
        assertThat(response.nome()).isEqualTo(request.nome());
        assertThat(response.descricao()).isEqualTo(request.descricao());
        assertThat(response.valorMensal()).isEqualByComparingTo(request.valorMensal());
        assertThat(response.duracaoEmDias()).isEqualTo(request.duracaoEmDias());
        verify(planoRepository).save(plano);
    }

    @Test
    void naoDeveAtualizarPlanoInexistente() {
        Long planoId = 99L;
        PlanoRequestDTO request = criarRequest("Plano Mensal", "Acesso livre por 30 dias");

        when(planoRepository.findById(planoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planoService.atualizar(planoId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plano nao encontrado com id: 99");

        verify(planoRepository, never()).existsByNomeAndIdNot(request.nome(), planoId);
        verify(planoRepository, never()).save(any(Plano.class));
    }

    @Test
    void naoDeveAtualizarPlanoUsandoNomeDeOutroPlano() {
        Long planoId = 1L;
        Plano plano = criarPlano(planoId, "Plano Mensal", "Acesso livre por 30 dias");
        PlanoRequestDTO request = criarRequest("Plano Anual", "Acesso livre por 365 dias");

        when(planoRepository.findById(planoId)).thenReturn(Optional.of(plano));
        when(planoRepository.existsByNomeAndIdNot(request.nome(), planoId)).thenReturn(true);

        assertThatThrownBy(() -> planoService.atualizar(planoId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe plano cadastrado com este nome");

        verify(planoRepository, never()).save(any(Plano.class));
    }

    @Test
    void deveDesativarPlanoComSucesso() {
        Long planoId = 1L;
        Plano plano = criarPlano(planoId, "Plano Mensal", "Acesso livre por 30 dias");

        when(planoRepository.findById(planoId)).thenReturn(Optional.of(plano));
        when(planoRepository.save(plano)).thenReturn(plano);

        planoService.remover(planoId);

        assertThat(plano.getAtivo()).isFalse();
        verify(planoRepository).save(plano);
        verify(planoRepository, never()).delete(any(Plano.class));
    }

    @Test
    void naoDeveDesativarPlanoInexistente() {
        Long planoId = 99L;

        when(planoRepository.findById(planoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planoService.remover(planoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plano nao encontrado com id: 99");

        verify(planoRepository, never()).save(any(Plano.class));
        verify(planoRepository, never()).delete(any(Plano.class));
    }

    private PlanoRequestDTO criarRequest(String nome, String descricao) {
        return new PlanoRequestDTO(
                nome,
                descricao,
                BigDecimal.valueOf(99.90),
                30
        );
    }

    private Plano criarPlano(Long id, String nome, String descricao) {
        return Plano.builder()
                .id(id)
                .nome(nome)
                .descricao(descricao)
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
