package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.matricula.MatriculaRequestDTO;
import com.extreme.gym.dto.matricula.MatriculaResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private PlanoRepository planoRepository;

    @InjectMocks
    private MatriculaService matriculaService;

    @Test
    void deveCriarMatriculaComSucesso() {
        Long alunoId = 1L;
        Long planoId = 1L;
        LocalDate dataInicio = LocalDate.of(2026, 5, 9);
        LocalDateTime dataCadastro = LocalDateTime.now();
        MatriculaRequestDTO request = new MatriculaRequestDTO(alunoId, planoId, dataInicio);
        Aluno aluno = criarAluno(alunoId);
        Plano plano = criarPlano(planoId, true);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(planoRepository.findById(planoId)).thenReturn(Optional.of(plano));
        when(matriculaRepository.existsByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA)).thenReturn(false);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> {
            Matricula matricula = invocation.getArgument(0);
            matricula.setId(1L);
            matricula.setDataCadastro(dataCadastro);
            return matricula;
        });

        MatriculaResponseDTO response = matriculaService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.alunoId()).isEqualTo(alunoId);
        assertThat(response.alunoNome()).isEqualTo(aluno.getNome());
        assertThat(response.planoId()).isEqualTo(planoId);
        assertThat(response.planoNome()).isEqualTo(plano.getNome());
        assertThat(response.dataInicio()).isEqualTo(dataInicio);
        assertThat(response.dataFim()).isEqualTo(dataInicio.plusDays(plano.getDuracaoEmDias()));
        assertThat(response.status()).isEqualTo(StatusMatricula.ATIVA);
        assertThat(response.dataCadastro()).isEqualTo(dataCadastro);
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void naoDeveCriarMatriculaParaAlunoInexistente() {
        Long alunoId = 99L;
        MatriculaRequestDTO request = new MatriculaRequestDTO(alunoId, 1L, LocalDate.of(2026, 5, 9));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.cadastrar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");

        verify(planoRepository, never()).findById(request.planoId());
        verify(matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    void naoDeveCriarMatriculaParaPlanoInexistente() {
        Long alunoId = 1L;
        Long planoId = 99L;
        MatriculaRequestDTO request = new MatriculaRequestDTO(alunoId, planoId, LocalDate.of(2026, 5, 9));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(criarAluno(alunoId)));
        when(planoRepository.findById(planoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.cadastrar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plano nao encontrado com id: 99");

        verify(matriculaRepository, never()).existsByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA);
        verify(matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    void naoDeveCriarMatriculaComPlanoInativo() {
        Long alunoId = 1L;
        Long planoId = 1L;
        MatriculaRequestDTO request = new MatriculaRequestDTO(alunoId, planoId, LocalDate.of(2026, 5, 9));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(criarAluno(alunoId)));
        when(planoRepository.findById(planoId)).thenReturn(Optional.of(criarPlano(planoId, false)));

        assertThatThrownBy(() -> matriculaService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Plano inativo nao pode ser usado em matricula");

        verify(matriculaRepository, never()).existsByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA);
        verify(matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    void naoDeveCriarMatriculaSeAlunoJaPossuiMatriculaAtiva() {
        Long alunoId = 1L;
        Long planoId = 1L;
        MatriculaRequestDTO request = new MatriculaRequestDTO(alunoId, planoId, LocalDate.of(2026, 5, 9));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(criarAluno(alunoId)));
        when(planoRepository.findById(planoId)).thenReturn(Optional.of(criarPlano(planoId, true)));
        when(matriculaRepository.existsByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA)).thenReturn(true);

        assertThatThrownBy(() -> matriculaService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Aluno ja possui matricula ativa");

        verify(matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    void deveListarMatriculas() {
        Matricula mensal = criarMatricula(1L, StatusMatricula.ATIVA);
        Matricula anual = criarMatricula(2L, StatusMatricula.CANCELADA);

        when(matriculaRepository.findAll()).thenReturn(List.of(mensal, anual));

        List<MatriculaResponseDTO> response = matriculaService.listar();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(MatriculaResponseDTO::id)
                .containsExactly(1L, 2L);
    }

    @Test
    void deveBuscarMatriculaPorId() {
        Long matriculaId = 1L;
        Matricula matricula = criarMatricula(matriculaId, StatusMatricula.ATIVA);

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.of(matricula));

        MatriculaResponseDTO response = matriculaService.buscarPorId(matriculaId);

        assertThat(response.id()).isEqualTo(matriculaId);
        assertThat(response.alunoId()).isEqualTo(matricula.getAluno().getId());
        assertThat(response.alunoNome()).isEqualTo(matricula.getAluno().getNome());
        assertThat(response.planoId()).isEqualTo(matricula.getPlano().getId());
        assertThat(response.planoNome()).isEqualTo(matricula.getPlano().getNome());
        assertThat(response.dataInicio()).isEqualTo(matricula.getDataInicio());
        assertThat(response.dataFim()).isEqualTo(matricula.getDataFim());
        assertThat(response.status()).isEqualTo(StatusMatricula.ATIVA);
        assertThat(response.dataCadastro()).isEqualTo(matricula.getDataCadastro());
    }

    @Test
    void deveLancarErroAoBuscarMatriculaInexistente() {
        Long matriculaId = 99L;

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.buscarPorId(matriculaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matricula nao encontrada com id: 99");
    }

    @Test
    void deveCancelarMatriculaComSucesso() {
        Long matriculaId = 1L;
        Matricula matricula = criarMatricula(matriculaId, StatusMatricula.ATIVA);

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDTO response = matriculaService.cancelar(matriculaId);

        assertThat(response.status()).isEqualTo(StatusMatricula.CANCELADA);
        assertThat(matricula.getStatus()).isEqualTo(StatusMatricula.CANCELADA);
        verify(matriculaRepository).save(matricula);
        verify(matriculaRepository, never()).delete(any(Matricula.class));
    }

    @Test
    void naoDeveCancelarMatriculaInexistente() {
        Long matriculaId = 99L;

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.cancelar(matriculaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matricula nao encontrada com id: 99");

        verify(matriculaRepository, never()).save(any(Matricula.class));
        verify(matriculaRepository, never()).delete(any(Matricula.class));
    }

    private Aluno criarAluno(Long id) {
        return Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana.silva@email.com")
                .telefone("71999990000")
                .status(StatusAluno.ATIVO)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Plano criarPlano(Long id, Boolean ativo) {
        return Plano.builder()
                .id(id)
                .nome("Plano Mensal")
                .descricao("Acesso livre por 30 dias")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(ativo)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Matricula criarMatricula(Long id, StatusMatricula status) {
        LocalDate dataInicio = LocalDate.of(2026, 5, 9);

        return Matricula.builder()
                .id(id)
                .aluno(criarAluno(1L))
                .plano(criarPlano(1L, true))
                .dataInicio(dataInicio)
                .dataFim(dataInicio.plusDays(30))
                .status(status)
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
