package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoRequestDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoCreatedResponseDTO;
import com.extreme.gym.dto.dispositivo.DispositivoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoLoteRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoLoteResponseDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.FormaPagamento;
import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.ModoOperacaoDispositivo;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.enums.TipoDispositivoAcesso;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AccessControlContractServiceTest {

    @Autowired
    private DispositivoAcessoService dispositivoService;

    @Autowired
    private DeviceApiKeyAuthenticator deviceApiKeyAuthenticator;

    @Autowired
    private CredencialAcessoService credencialService;

    @Autowired
    private ControleAcessoService controleAcessoService;

    @Autowired
    private EventoAcessoService eventoAcessoService;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Test
    void deveExecutarContratoTecnicoComApiKeySnapshotValidacaoEIdempotencia() {
        String suffix = String.valueOf(System.nanoTime());
        Aluno aluno = criarAlunoComMatriculaPaga(suffix);
        DispositivoAcessoCreatedResponseDTO dispositivoCriado = dispositivoService.criar(new DispositivoAcessoRequestDTO(
                "Gateway teste " + suffix,
                TipoDispositivoAcesso.GATEWAY,
                ModoOperacaoDispositivo.HIBRIDO,
                "gateway-test-" + suffix,
                null,
                null,
                null,
                "Recepcao"
        ));

        assertThat(dispositivoCriado.apiKeyPlaintext()).isNotBlank();
        assertThat(dispositivoService.listar())
                .filteredOn(dispositivo -> dispositivo.id().equals(dispositivoCriado.dispositivo().id()))
                .first()
                .extracting("nome")
                .isEqualTo("Gateway teste " + suffix);

        assertThatThrownBy(() -> deviceApiKeyAuthenticator.authenticate(
                dispositivoCriado.dispositivo().id(),
                "api-key-invalida"
        )).isInstanceOf(BadCredentialsException.class);

        DispositivoAcesso dispositivo = deviceApiKeyAuthenticator.authenticate(
                dispositivoCriado.dispositivo().id(),
                dispositivoCriado.apiKeyPlaintext()
        );

        String credencialExterna = "qr-" + suffix;
        credencialService.criar(aluno.getId(), new CredencialAcessoRequestDTO(
                TipoCredencialAcesso.QR_CODE,
                credencialExterna,
                "smoke-test",
                LocalDateTime.now(),
                "v1"
        ));

        SnapshotAutorizadosResponseDTO snapshot = controleAcessoService.gerarSnapshot(dispositivo);
        assertThat(snapshot.itens())
                .anySatisfy(item -> {
                    assertThat(item.alunoId()).isEqualTo(aluno.getId());
                    assertThat(item.identificadorExterno()).isEqualTo(credencialExterna);
                    assertThat(item.liberado()).isTrue();
                    assertThat(item.motivoBloqueio()).isNull();
                });

        ValidarDispositivoResponseDTO validacao = controleAcessoService.validarDispositivo(
                dispositivo,
                new ValidarDispositivoRequestDTO(
                        TipoCredencialAcesso.QR_CODE,
                        credencialExterna,
                        OrigemEventoAcesso.DISPOSITIVO,
                        "validacao-" + suffix,
                        LocalDateTime.now()
                )
        );

        assertThat(validacao.permitido()).isTrue();
        assertThat(validacao.resultado()).isEqualTo(ResultadoAcesso.LIBERADO);
        assertThat(validacao.alunoId()).isEqualTo(aluno.getId());
        assertThat(validacao.eventoId()).isNotNull();

        EventoAcessoRequestDTO evento = new EventoAcessoRequestDTO(
                "evento-offline-" + suffix,
                aluno.getId(),
                TipoCredencialAcesso.QR_CODE,
                credencialExterna,
                ResultadoAcesso.LIBERADO,
                "Acesso liberado offline",
                ModoEventoAcesso.OFFLINE,
                OrigemEventoAcesso.DISPOSITIVO,
                LocalDateTime.now(),
                "offline-" + suffix
        );

        EventoAcessoLoteResponseDTO primeiroLote = eventoAcessoService.sincronizarLote(
                dispositivo,
                new EventoAcessoLoteRequestDTO(List.of(evento))
        );
        EventoAcessoLoteResponseDTO segundoLote = eventoAcessoService.sincronizarLote(
                dispositivo,
                new EventoAcessoLoteRequestDTO(List.of(evento))
        );

        assertThat(primeiroLote.totalCriados()).isEqualTo(1);
        assertThat(primeiroLote.totalDuplicados()).isZero();
        assertThat(segundoLote.totalCriados()).isZero();
        assertThat(segundoLote.totalDuplicados()).isEqualTo(1);
    }

    private Aluno criarAlunoComMatriculaPaga(String suffix) {
        Aluno aluno = alunoRepository.save(Aluno.builder()
                .nome("Aluno contrato " + suffix)
                .email("contrato-" + suffix + "@example.com")
                .telefone("71999990000")
                .status(StatusAluno.ATIVO)
                .build());

        Plano plano = planoRepository.save(Plano.builder()
                .nome("Plano contrato " + suffix)
                .descricao("Plano para teste de contrato")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .build());

        Matricula matricula = matriculaRepository.save(Matricula.builder()
                .aluno(aluno)
                .plano(plano)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .status(StatusMatricula.ATIVA)
                .build());

        pagamentoRepository.save(Pagamento.builder()
                .matricula(matricula)
                .valor(BigDecimal.valueOf(99.90))
                .formaPagamento(FormaPagamento.PIX)
                .status(StatusPagamento.PAGO)
                .build());

        return aluno;
    }
}
