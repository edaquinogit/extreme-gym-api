package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.buscaglobal.BuscaGlobalResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BuscaGlobalServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PlanoRepository planoRepository;

    @InjectMocks
    private BuscaGlobalService buscaGlobalService;

    @Test
    void naoDeveConsultarRepositoriosQuandoTermoForMenorQueDoisCaracteres() {
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", true);

        BuscaGlobalResponseDTO response = buscaGlobalService.buscar("A", 5, autenticacao("ROLE_ADMIN"));

        assertThat(response.alunos()).isEmpty();
        assertThat(response.matriculas()).isEmpty();
        assertThat(response.pagamentos()).isEmpty();
        assertThat(response.planos()).isEmpty();
        verify(alunoRepository, never()).buscarResumoGlobal(any(), any());
    }

    @Test
    void naoDeveConsultarRepositoriosQuandoBuscaGlobalEstiverDesabilitada() {
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", false);

        BuscaGlobalResponseDTO response = buscaGlobalService.buscar("Ana", 5, autenticacao("ROLE_ADMIN"));

        assertThat(response.alunos()).isEmpty();
        verify(alunoRepository, never()).buscarResumoGlobal(any(), any());
    }

    @Test
    void deveAgregarResultadosDeTodasAsEntidadesParaPerfilAdmin() {
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", true);

        Aluno aluno = Aluno.builder().id(1L).nome("Ana Souza").email("ana@email.com").status(StatusAluno.ATIVO).build();
        Plano plano = Plano.builder().id(2L).nome("Plano Ana").descricao("desc").valorMensal(BigDecimal.TEN).duracaoEmDias(30).ativo(true).build();
        Matricula matricula = Matricula.builder().id(3L).aluno(aluno).plano(plano).status(StatusMatricula.ATIVA).build();
        Pagamento pagamento = Pagamento.builder().id(4L).matricula(matricula).valor(BigDecimal.TEN).status(StatusPagamento.PAGO).build();

        when(alunoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(aluno));
        when(matriculaRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(matricula));
        when(planoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(plano));
        when(pagamentoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(pagamento));

        BuscaGlobalResponseDTO response = buscaGlobalService.buscar("Ana", 5, autenticacao("ROLE_ADMIN"));

        assertThat(response.alunos()).hasSize(1);
        assertThat(response.alunos().get(0).tipo()).isEqualTo("ALUNO");
        assertThat(response.alunos().get(0).rota()).isEqualTo("/alunos/1");
        assertThat(response.matriculas()).hasSize(1);
        assertThat(response.matriculas().get(0).subtitulo()).isEqualTo("Ana Souza");
        assertThat(response.planos()).hasSize(1);
        assertThat(response.pagamentos()).hasSize(1);
        assertThat(response.pagamentos().get(0).subtitulo()).isEqualTo("Ana Souza");
    }

    @Test
    void devOcultarPagamentosParaPerfilCatraca() {
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", true);

        Aluno aluno = Aluno.builder().id(1L).nome("Ana Souza").email("ana@email.com").status(StatusAluno.ATIVO).build();
        when(alunoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(aluno));
        when(matriculaRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());
        when(planoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());

        BuscaGlobalResponseDTO response = buscaGlobalService.buscar("Ana", 5, autenticacao("ROLE_CATRACA"));

        assertThat(response.alunos()).hasSize(1);
        assertThat(response.pagamentos()).isEmpty();
        verify(pagamentoRepository, never()).buscarResumoGlobal(any(), any());
    }

    @Test
    void deveTratarAutenticacaoNulaComoPerfilSemRestricao() {
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", true);

        when(alunoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());
        when(matriculaRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());
        when(planoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());
        when(pagamentoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());

        BuscaGlobalResponseDTO response = buscaGlobalService.buscar("Ana", 5, null);

        assertThat(response.pagamentos()).isEmpty();
        verify(pagamentoRepository).buscarResumoGlobal(eq("Ana"), any(Pageable.class));
    }

    private Authentication autenticacao(String role) {
        return new TestingAuthenticationToken("usuario", "senha", role);
    }
}
