package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcessoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private AcessoService acessoService;

    @Test
    void deveLiberarAcessoComAlunoAtivoMatriculaAtivaNaoVencidaEPagamentoPago() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().plusDays(30));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO))
                .thenReturn(true);

        AcessoResponseDTO response = acessoService.validar(new AcessoRequestDTO(alunoId));

        assertThat(response.alunoId()).isEqualTo(alunoId);
        assertThat(response.alunoNome()).isEqualTo(aluno.getNome());
        assertThat(response.acessoLiberado()).isTrue();
        assertThat(response.motivo()).isEqualTo("Acesso liberado");
        assertThat(response.matriculaId()).isEqualTo(matricula.getId());
        assertThat(response.dataValidadeMatricula()).isEqualTo(matricula.getDataFim());
    }

    @Test
    void deveLancarErroParaAlunoInexistente() {
        Long alunoId = 99L;

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> acessoService.validar(new AcessoRequestDTO(alunoId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");

        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearAlunoBloqueado() {
        AcessoResponseDTO response = validarAcessoBloqueadoPorStatus(StatusAluno.BLOQUEADO);

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Aluno bloqueado");
        assertThat(response.matriculaId()).isNull();
        assertThat(response.dataValidadeMatricula()).isNull();
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearAlunoCancelado() {
        AcessoResponseDTO response = validarAcessoBloqueadoPorStatus(StatusAluno.CANCELADO);

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Aluno cancelado");
        assertThat(response.matriculaId()).isNull();
        assertThat(response.dataValidadeMatricula()).isNull();
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearAlunoInadimplente() {
        AcessoResponseDTO response = validarAcessoBloqueadoPorStatus(StatusAluno.INADIMPLENTE);

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Aluno inadimplente");
        assertThat(response.matriculaId()).isNull();
        assertThat(response.dataValidadeMatricula()).isNull();
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearAlunoSemMatriculaAtiva() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.empty());

        AcessoResponseDTO response = acessoService.validar(new AcessoRequestDTO(alunoId));

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Aluno nao possui matricula ativa");
        assertThat(response.matriculaId()).isNull();
        assertThat(response.dataValidadeMatricula()).isNull();
        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearMatriculaVencida() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().minusDays(1));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));

        AcessoResponseDTO response = acessoService.validar(new AcessoRequestDTO(alunoId));

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Matricula vencida");
        assertThat(response.matriculaId()).isEqualTo(matricula.getId());
        assertThat(response.dataValidadeMatricula()).isEqualTo(matricula.getDataFim());
        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearMatriculaSemPagamentoPago() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().plusDays(30));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO))
                .thenReturn(false);

        AcessoResponseDTO response = acessoService.validar(new AcessoRequestDTO(alunoId));

        assertThat(response.acessoLiberado()).isFalse();
        assertThat(response.motivo()).isEqualTo("Matricula nao possui pagamento pago");
        assertThat(response.matriculaId()).isEqualTo(matricula.getId());
        assertThat(response.dataValidadeMatricula()).isEqualTo(matricula.getDataFim());
    }

    private AcessoResponseDTO validarAcessoBloqueadoPorStatus(StatusAluno status) {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, status);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        return acessoService.validar(new AcessoRequestDTO(alunoId));
    }

    private Aluno criarAluno(Long id, StatusAluno status) {
        return Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana.silva@email.com")
                .telefone("71999990000")
                .status(status)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Matricula criarMatricula(Long id, Aluno aluno, LocalDate dataFim) {
        return Matricula.builder()
                .id(id)
                .aluno(aluno)
                .plano(criarPlano(1L))
                .dataInicio(LocalDate.now())
                .dataFim(dataFim)
                .status(StatusMatricula.ATIVA)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Plano criarPlano(Long id) {
        return Plano.builder()
                .id(id)
                .nome("Plano Mensal")
                .descricao("Acesso livre por 30 dias")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
