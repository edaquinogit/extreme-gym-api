package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinRequestDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinResponseDTO;
import com.extreme.gym.dto.acesso.FichaAcessoResponseDTO;
import com.extreme.gym.dto.acesso.LiberacaoManualRequestDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.FormaPagamento;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import com.extreme.gym.repository.EventoAcessoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private CredencialAcessoRepository credencialRepository;

    @Mock
    private EventoAcessoRepository eventoAcessoRepository;

    @Mock
    private CheckInRepository checkInRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneId.of("America/Bahia"));

    private AcessoService acessoService;

    @BeforeEach
    void setUp() {
        acessoService = new AcessoService(
                alunoRepository,
                matriculaRepository,
                pagamentoRepository,
                credencialRepository,
                eventoAcessoRepository,
                checkInRepository,
                clock
        );
    }

    @Test
    void deveLiberarAcessoComAlunoAtivoMatriculaAtivaNaoVencidaEPagamentoPago() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, today().plusDays(30));

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
        Matricula matricula = criarMatricula(1L, aluno, today().minusDays(1));

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
        Matricula matricula = criarMatricula(1L, aluno, today().plusDays(30));

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

    @Test
    void deveValidarPinAssistidoSemExporCredencial() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        CredencialAcesso pin = CredencialAcesso.builder()
                .id(10L)
                .alunoId(alunoId)
                .tipo(TipoCredencialAcesso.PIN)
                .identificadorExterno("123456")
                .status(StatusCredencialAcesso.ATIVA)
                .build();

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(credencialRepository.findByAlunoIdAndTipoAndStatus(
                alunoId,
                TipoCredencialAcesso.PIN,
                StatusCredencialAcesso.ATIVA
        )).thenReturn(Optional.of(pin));

        AssistenciaPinResponseDTO response = acessoService.validarPinAssistido(
                new AssistenciaPinRequestDTO(alunoId, "123456")
        );

        assertThat(response.valido()).isTrue();
        assertThat(response.alunoId()).isEqualTo(alunoId);
        assertThat(response.alunoNome()).isEqualTo("Ana Silva");
    }

    @Test
    void deveRejeitarPinAssistidoIncorreto() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        CredencialAcesso pin = CredencialAcesso.builder()
                .alunoId(alunoId)
                .tipo(TipoCredencialAcesso.PIN)
                .identificadorExterno("123456")
                .status(StatusCredencialAcesso.ATIVA)
                .build();

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(credencialRepository.findByAlunoIdAndTipoAndStatus(
                alunoId,
                TipoCredencialAcesso.PIN,
                StatusCredencialAcesso.ATIVA
        )).thenReturn(Optional.of(pin));

        AssistenciaPinResponseDTO response = acessoService.validarPinAssistido(
                new AssistenciaPinRequestDTO(alunoId, "000000")
        );

        assertThat(response.valido()).isFalse();
    }

    @Test
    void deveMontarFichaAgregadaSemIdentificadorDaCredencial() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(5L, aluno, today().plusDays(30));
        Pagamento pagamento = criarPagamento(7L, matricula, StatusPagamento.PAGO, LocalDateTime.now(clock));
        CredencialAcesso pin = CredencialAcesso.builder()
                .id(9L)
                .alunoId(alunoId)
                .tipo(TipoCredencialAcesso.PIN)
                .identificadorExterno("123456")
                .fornecedor("SISTEMA")
                .status(StatusCredencialAcesso.ATIVA)
                .cadastradoEm(LocalDateTime.now(clock))
                .build();

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.findByMatriculaId(matricula.getId())).thenReturn(List.of(pagamento));
        when(credencialRepository.findByAlunoIdAndStatus(alunoId, StatusCredencialAcesso.ATIVA))
                .thenReturn(List.of(pin));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO))
                .thenReturn(true);

        FichaAcessoResponseDTO ficha = acessoService.buscarFichaAcesso(alunoId);

        assertThat(ficha.aluno().id()).isEqualTo(alunoId);
        assertThat(ficha.matriculaAtiva().id()).isEqualTo(matricula.getId());
        assertThat(ficha.ultimoPagamento().id()).isEqualTo(pagamento.getId());
        assertThat(ficha.credenciaisAtivas()).hasSize(1);
        assertThat(ficha.credenciaisAtivas().getFirst().tipo()).isEqualTo(TipoCredencialAcesso.PIN);
        assertThat(ficha.restricoes()).isEmpty();
        assertThat(ficha.acoesPermitidas()).contains("BLOQUEAR");
    }

    @Test
    void deveRegistrarLiberacaoManualComEventoECheckinQuandoSolicitado() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(5L, aluno, today().plusDays(30));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(eventoAcessoRepository.save(any(EventoAcesso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AcessoResponseDTO response = acessoService.liberarManual(
                new LiberacaoManualRequestDTO(alunoId, "Pagamento confirmado", "Comprovante apresentado", true)
        );

        assertThat(response.acessoLiberado()).isTrue();
        assertThat(response.motivo()).contains("Pagamento confirmado");
        assertThat(response.matriculaId()).isEqualTo(matricula.getId());
        verify(eventoAcessoRepository).save(any(EventoAcesso.class));
        verify(checkInRepository).save(any(CheckIn.class));
    }

    private AcessoResponseDTO validarAcessoBloqueadoPorStatus(StatusAluno status) {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, status);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        return acessoService.validar(new AcessoRequestDTO(alunoId));
    }

    private Aluno criarAluno(Long id, StatusAluno status) {
        Aluno aluno = Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana.silva@email.com")
                .telefone("71999990000")
                .status(status)
                .build();
        aluno.setCriadoEm(LocalDateTime.now(clock));
        return aluno;
    }

    private Matricula criarMatricula(Long id, Aluno aluno, LocalDate dataFim) {
        return Matricula.builder()
                .id(id)
                .aluno(aluno)
                .plano(criarPlano(1L))
                .dataInicio(today())
                .dataFim(dataFim)
                .status(StatusMatricula.ATIVA)
                .dataCadastro(LocalDateTime.now(clock))
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
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }

    private Pagamento criarPagamento(Long id, Matricula matricula, StatusPagamento status, LocalDateTime dataPagamento) {
        return Pagamento.builder()
                .id(id)
                .matricula(matricula)
                .valor(BigDecimal.valueOf(99.90))
                .formaPagamento(FormaPagamento.PIX)
                .status(status)
                .dataPagamento(dataPagamento)
                .dataCadastro(dataPagamento.minusMinutes(5))
                .build();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
