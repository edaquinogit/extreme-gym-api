package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.buscaglobal.BuscaGlobalResponseDTO;
import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoHeartbeatRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.StatusDispositivoAcesso;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import com.extreme.gym.repository.DispositivoAcessoRepository;
import com.extreme.gym.repository.EventoAcessoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AccessControlContractServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-02T12:00:00Z"), ZoneId.of("America/Bahia"));
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private DispositivoAcessoRepository dispositivoRepository;

    @Mock
    private EventoAcessoRepository eventoRepository;

    @Mock
    private CredencialAcessoRepository credencialRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PlanoRepository planoRepository;

    @Mock
    private AuditoriaAcessoService auditoriaAcessoService;

    private DispositivoAcessoService dispositivoService;
    private EventoAcessoService eventoService;
    private AcessoService acessoService;
    private ControleAcessoService controleAcessoService;
    private BuscaGlobalService buscaGlobalService;

    @BeforeEach
    void setUp() {
        dispositivoService = new DispositivoAcessoService(
                dispositivoRepository,
                passwordEncoder,
                clock,
                auditoriaAcessoService
        );
        eventoService = new EventoAcessoService(eventoRepository, dispositivoService, auditoriaAcessoService, clock);
        acessoService = new AcessoService(alunoRepository, matriculaRepository, pagamentoRepository, clock);
        controleAcessoService = new ControleAcessoService(
                dispositivoService,
                credencialRepository,
                acessoService,
                eventoService,
                auditoriaAcessoService,
                clock
        );
        ReflectionTestUtils.setField(controleAcessoService, "moduloHabilitado", true);
        ReflectionTestUtils.setField(controleAcessoService, "apiKeyObrigatoria", false);
        ReflectionTestUtils.setField(controleAcessoService, "snapshotHabilitado", true);
        ReflectionTestUtils.setField(controleAcessoService, "validadeSnapshotMinutos", 15L);

        buscaGlobalService = new BuscaGlobalService(alunoRepository, matriculaRepository, pagamentoRepository, planoRepository);
        ReflectionTestUtils.setField(buscaGlobalService, "buscaGlobalHabilitada", true);
    }

    @Test
    void deveCriarDispositivoValidoSemExporApiKey() {
        when(dispositivoRepository.existsByNomeIgnoreCase("Catraca entrada")).thenReturn(false);
        when(dispositivoRepository.save(any(DispositivoAcesso.class))).thenAnswer(invocation -> {
            DispositivoAcesso dispositivo = invocation.getArgument(0);
            dispositivo.setId(1L);
            dispositivo.setCriadoEm(LocalDateTime.now(clock));
            dispositivo.setAtualizadoEm(LocalDateTime.now(clock));
            return dispositivo;
        });

        DispositivoAcessoResponseDTO response = dispositivoService.criar(new DispositivoAcessoRequestDTO(
                "Catraca entrada",
                TipoDispositivoAcesso.CATRACA_QR,
                "Fabricante pendente",
                null,
                "catraca-entrada",
                "192.168.0.10",
                "Unidade principal",
                StatusDispositivoAcesso.ATIVO,
                ModoOperacaoDispositivo.HIBRIDO,
                "segredo-tecnico"
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Catraca entrada");
        verify(dispositivoRepository).save(any(DispositivoAcesso.class));
    }

    @Test
    void naoDeveCriarDispositivoComNomeDuplicado() {
        when(dispositivoRepository.existsByNomeIgnoreCase("Catraca entrada")).thenReturn(true);

        assertThatThrownBy(() -> dispositivoService.criar(new DispositivoAcessoRequestDTO(
                "Catraca entrada",
                TipoDispositivoAcesso.CATRACA_QR,
                null,
                null,
                null,
                null,
                null,
                StatusDispositivoAcesso.ATIVO,
                ModoOperacaoDispositivo.ONLINE,
                null
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe dispositivo de acesso com este nome");

        verify(dispositivoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarHeartbeat() {
        DispositivoAcesso dispositivo = dispositivo(1L, StatusDispositivoAcesso.ATIVO);
        when(dispositivoRepository.findById(1L)).thenReturn(Optional.of(dispositivo));
        when(dispositivoRepository.save(dispositivo)).thenReturn(dispositivo);

        DispositivoAcessoResponseDTO response = dispositivoService.heartbeat(1L, new DispositivoHeartbeatRequestDTO(null));

        assertThat(response.ultimaComunicacaoEm()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void dispositivoInativoNaoDeveValidarAcesso() {
        DispositivoAcesso dispositivo = dispositivo(1L, StatusDispositivoAcesso.INATIVO);
        when(dispositivoRepository.findById(1L)).thenReturn(Optional.of(dispositivo));

        assertThatThrownBy(() -> controleAcessoService.validarDispositivo(validarRequest(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Dispositivo de acesso nao esta operacional");

        verify(credencialRepository, never()).findByTipoAndIdentificadorExternoAndStatus(any(), any(), any());
    }

    @Test
    void deveRegistrarEventoLiberadoEBloqueadoComIdempotencia() {
        DispositivoAcesso dispositivo = dispositivo(1L, StatusDispositivoAcesso.ATIVO);
        EventoAcesso liberado = evento(10L, ResultadoAcesso.LIBERADO, "idem-1");
        EventoAcesso bloqueado = evento(11L, ResultadoAcesso.BLOQUEADO, "idem-2");

        when(eventoRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(eventoRepository.findByIdempotencyKey("idem-2")).thenReturn(Optional.of(bloqueado));
        when(dispositivoRepository.findById(1L)).thenReturn(Optional.of(dispositivo));
        when(eventoRepository.save(any(EventoAcesso.class))).thenReturn(liberado);

        EventoAcessoResponseDTO novo = eventoService.registrar(eventoRequest(ResultadoAcesso.LIBERADO, "idem-1"));
        EventoAcessoResponseDTO duplicado = eventoService.registrar(eventoRequest(ResultadoAcesso.BLOQUEADO, "idem-2"));

        assertThat(novo.resultado()).isEqualTo(ResultadoAcesso.LIBERADO);
        assertThat(duplicado.resultado()).isEqualTo(ResultadoAcesso.BLOQUEADO);
        assertThat(duplicado.duplicado()).isTrue();
    }

    @Test
    void deveValidarPorDispositivoReutilizandoRegraAtual() {
        DispositivoAcesso dispositivo = dispositivo(1L, StatusDispositivoAcesso.ATIVO);
        Aluno aluno = aluno(2L);
        Plano plano = plano(3L);
        Matricula matricula = matricula(4L, aluno, plano);
        CredencialAcesso credencial = credencial(5L, aluno.getId(), StatusCredencialAcesso.ATIVA);
        EventoAcesso evento = evento(6L, ResultadoAcesso.LIBERADO, "idem-validar");

        when(dispositivoRepository.findById(1L)).thenReturn(Optional.of(dispositivo));
        when(credencialRepository.findByTipoAndIdentificadorExternoAndStatus(
                TipoCredencialAcesso.QR_CODE,
                "qr-ana",
                StatusCredencialAcesso.ATIVA
        )).thenReturn(Optional.of(credencial));
        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(eq(matricula.getId()), any())).thenReturn(true);
        when(eventoRepository.findByIdempotencyKey("idem-validar")).thenReturn(Optional.empty());
        when(eventoRepository.save(any(EventoAcesso.class))).thenReturn(evento);

        ValidarDispositivoResponseDTO response = controleAcessoService.validarDispositivo(validarRequest(), null);

        assertThat(response.permitido()).isTrue();
        assertThat(response.resultado()).isEqualTo(ResultadoAcesso.LIBERADO);
        assertThat(response.aluno().id()).isEqualTo(aluno.getId());
        assertThat(response.matricula().id()).isEqualTo(matricula.getId());
        assertThat(response.plano().id()).isEqualTo(plano.getId());
    }

    @Test
    void snapshotDeveRetornarCamposMinimosEBloqueioDeCredencialRevogada() {
        CredencialAcesso ativa = credencial(1L, 2L, StatusCredencialAcesso.ATIVA);
        CredencialAcesso revogada = credencial(2L, 3L, StatusCredencialAcesso.REVOGADA);
        Aluno aluno = aluno(2L);
        Matricula matricula = matricula(4L, aluno, plano(5L));

        when(credencialRepository.findByStatus(StatusCredencialAcesso.ATIVA)).thenReturn(List.of(ativa));
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(2L, StatusMatricula.ATIVA)).thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(eq(4L), any())).thenReturn(true);

        SnapshotAutorizadosResponseDTO snapshot = controleAcessoService.gerarSnapshot(null);

        assertThat(snapshot.totalCredenciais()).isEqualTo(1);
        assertThat(snapshot.totalLiberados()).isEqualTo(1);
        assertThat(snapshot.itens()).singleElement()
                .extracting(SnapshotAutorizadosResponseDTO.Item::identificadorExterno)
                .isEqualTo("qr-ana");
        assertThat(revogada.getStatus()).isEqualTo(StatusCredencialAcesso.REVOGADA);
    }

    @Test
    void buscaGlobalExigeTermoMinimoERespeitaPerfilCatraca() {
        Aluno aluno = aluno(2L);
        when(alunoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of(aluno));
        when(matriculaRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());
        when(planoRepository.buscarResumoGlobal(eq("Ana"), any(Pageable.class))).thenReturn(List.of());

        BuscaGlobalResponseDTO insuficiente = buscaGlobalService.buscar("A", 5, null);
        BuscaGlobalResponseDTO catraca = buscaGlobalService.buscar(
                "Ana",
                5,
                new TestingAuthenticationToken("catraca", "senha", "ROLE_CATRACA")
        );

        assertThat(insuficiente.alunos()).isEmpty();
        assertThat(catraca.alunos()).hasSize(1);
        assertThat(catraca.pagamentos()).isEmpty();
        verify(pagamentoRepository, never()).buscarResumoGlobal(any(), any());
    }

    private ValidarDispositivoRequestDTO validarRequest() {
        return new ValidarDispositivoRequestDTO(
                1L,
                TipoCredencialAcesso.QR_CODE,
                "qr-ana",
                OrigemEventoAcesso.QR_CODE,
                "idem-validar",
                LocalDateTime.now(clock)
        );
    }

    private EventoAcessoRequestDTO eventoRequest(ResultadoAcesso resultado, String idempotencyKey) {
        return new EventoAcessoRequestDTO(
                2L,
                1L,
                resultado == ResultadoAcesso.LIBERADO ? 4L : null,
                OrigemEventoAcesso.GATEWAY,
                ModoEventoAcesso.ONLINE,
                resultado,
                resultado == ResultadoAcesso.LIBERADO ? "Acesso liberado" : "Aluno bloqueado",
                LocalDateTime.now(clock),
                true,
                null,
                idempotencyKey
        );
    }

    private DispositivoAcesso dispositivo(Long id, StatusDispositivoAcesso status) {
        return DispositivoAcesso.builder()
                .id(id)
                .nome("Catraca entrada")
                .tipo(TipoDispositivoAcesso.CATRACA_QR)
                .status(status)
                .modoOperacao(ModoOperacaoDispositivo.HIBRIDO)
                .criadoEm(LocalDateTime.now(clock))
                .atualizadoEm(LocalDateTime.now(clock))
                .build();
    }

    private EventoAcesso evento(Long id, ResultadoAcesso resultado, String idempotencyKey) {
        return EventoAcesso.builder()
                .id(id)
                .alunoId(2L)
                .dispositivoId(1L)
                .matriculaId(resultado == ResultadoAcesso.LIBERADO ? 4L : null)
                .origem(OrigemEventoAcesso.GATEWAY)
                .modo(ModoEventoAcesso.ONLINE)
                .resultado(resultado)
                .motivo(resultado == ResultadoAcesso.LIBERADO ? "Acesso liberado" : "Aluno bloqueado")
                .dataHoraEvento(LocalDateTime.now(clock))
                .dataHoraRecebimento(LocalDateTime.now(clock))
                .sincronizado(true)
                .idempotencyKey(idempotencyKey)
                .criadoEm(LocalDateTime.now(clock))
                .build();
    }

    private CredencialAcesso credencial(Long id, Long alunoId, StatusCredencialAcesso status) {
        return CredencialAcesso.builder()
                .id(id)
                .alunoId(alunoId)
                .tipo(TipoCredencialAcesso.QR_CODE)
                .identificadorExterno("qr-ana")
                .status(status)
                .cadastradoEm(LocalDateTime.now(clock))
                .criadoEm(LocalDateTime.now(clock))
                .atualizadoEm(LocalDateTime.now(clock))
                .build();
    }

    private Aluno aluno(Long id) {
        return Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana@email.com")
                .telefone("71999990000")
                .status(StatusAluno.ATIVO)
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }

    private Matricula matricula(Long id, Aluno aluno, Plano plano) {
        return Matricula.builder()
                .id(id)
                .aluno(aluno)
                .plano(plano)
                .dataInicio(LocalDate.now(clock))
                .dataFim(LocalDate.now(clock).plusDays(30))
                .status(StatusMatricula.ATIVA)
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }

    private Plano plano(Long id) {
        return Plano.builder()
                .id(id)
                .nome("Plano Mensal")
                .descricao("Acesso por 30 dias")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }
}
