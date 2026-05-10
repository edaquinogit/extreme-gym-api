package com.extreme.gym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CheckInControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PlanoRepository planoRepository;

    @BeforeEach
    void setUp() {
        checkInRepository.deleteAll();
        pagamentoRepository.deleteAll();
        matriculaRepository.deleteAll();
        alunoRepository.deleteAll();
        planoRepository.deleteAll();
    }

    @Test
    void deveRegistrarCheckInPermitidoParaAlunoAtivoComMatriculaAtivaNaoVencidaEPagamentoPago() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);
        criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson(alunoId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.permitido").value(true))
                .andExpect(jsonPath("$.motivo").value("Check-in permitido"))
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.aluno").doesNotExist())
                .andExpect(jsonPath("$.matricula").doesNotExist());

        assertThat(checkInRepository.count()).isEqualTo(1);
        CheckIn checkIn = checkInRepository.findAll().getFirst();
        assertThat(checkIn.getPermitido()).isTrue();
        assertThat(checkIn.getAluno().getId()).isEqualTo(alunoId);
        assertThat(checkIn.getMatricula().getId()).isEqualTo(matriculaId);
    }

    @Test
    void deveRegistrarTentativaBloqueadaQuandoAlunoNaoPossuiAcessoPermitido() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson(alunoId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.permitido").value(false))
                .andExpect(jsonPath("$.motivo").value("Aluno nao possui matricula ativa"))
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.aluno").doesNotExist())
                .andExpect(jsonPath("$.matricula").doesNotExist());

        assertThat(checkInRepository.count()).isEqualTo(1);
        CheckIn checkIn = checkInRepository.findAll().getFirst();
        assertThat(checkIn.getPermitido()).isFalse();
        assertThat(checkIn.getAluno().getId()).isEqualTo(alunoId);
        assertThat(checkIn.getMatricula()).isNull();
        assertThat(checkIn.getMotivo()).isEqualTo("Aluno nao possui matricula ativa");
    }

    @Test
    void deveListarCheckIns() throws Exception {
        Long matriculaAnaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoAnaId = buscarAlunoIdPorMatricula(matriculaAnaId);
        Long alunoBrunoId = criarAluno("Bruno Souza", "bruno.souza@email.com", "11888888888");
        criarPagamento(matriculaAnaId, "99.90", "PIX");

        registrarCheckIn(alunoAnaId);
        registrarCheckIn(alunoBrunoId);

        mockMvc.perform(get("/checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].alunoId", containsInAnyOrder(
                        alunoAnaId.intValue(),
                        alunoBrunoId.intValue()
                )))
                .andExpect(jsonPath("$[*].permitido", containsInAnyOrder(true, false)))
                .andExpect(jsonPath("$[*].aluno").doesNotExist())
                .andExpect(jsonPath("$[*].matricula").doesNotExist());
    }

    @Test
    void deveBuscarCheckInExistentePorId() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);
        criarPagamento(matriculaId, "99.90", "PIX");
        Long checkInId = registrarCheckIn(alunoId);

        mockMvc.perform(get("/checkins/{id}", checkInId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checkInId))
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.permitido").value(true))
                .andExpect(jsonPath("$.motivo").value("Check-in permitido"))
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.aluno").doesNotExist())
                .andExpect(jsonPath("$.matricula").doesNotExist());
    }

    @Test
    void deveListarCheckInsPorAluno() throws Exception {
        Long matriculaAnaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoAnaId = buscarAlunoIdPorMatricula(matriculaAnaId);
        Long alunoBrunoId = criarAluno("Bruno Souza", "bruno.souza@email.com", "11888888888");
        criarPagamento(matriculaAnaId, "99.90", "PIX");

        registrarCheckIn(alunoAnaId);
        registrarCheckIn(alunoBrunoId);

        mockMvc.perform(get("/checkins/aluno/{alunoId}", alunoAnaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].alunoId").value(alunoAnaId))
                .andExpect(jsonPath("$[0].alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$[0].permitido").value(true))
                .andExpect(jsonPath("$[0].motivo").value("Check-in permitido"))
                .andExpect(jsonPath("$[0].aluno").doesNotExist())
                .andExpect(jsonPath("$[0].matricula").doesNotExist());
    }

    @Test
    void deveRetornarErroQuandoAlunoForInexistente() throws Exception {
        mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Aluno nao encontrado com id: 999999"))
                .andExpect(jsonPath("$.path").value("/checkins"));

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoBloqueadoERegistrarTentativa() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.BLOQUEADO);

        registrarCheckInBloqueado(alunoId, "Aluno bloqueado");
    }

    @Test
    void deveBloquearAlunoCanceladoERegistrarTentativa() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.CANCELADO);

        registrarCheckInBloqueado(alunoId, "Aluno cancelado");
    }

    @Test
    void deveBloquearAlunoInadimplenteERegistrarTentativa() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.INADIMPLENTE);

        registrarCheckInBloqueado(alunoId, "Aluno inadimplente");
    }

    @Test
    void deveBloquearAlunoSemMatriculaAtivaERegistrarTentativa() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        registrarCheckInBloqueado(alunoId, "Aluno nao possui matricula ativa");
    }

    @Test
    void deveBloquearMatriculaVencidaERegistrarTentativa() throws Exception {
        Long matriculaId = criarMatriculaVencida();
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);

        registrarCheckInBloqueado(alunoId, "Matricula vencida");
    }

    @Test
    void deveBloquearMatriculaSemPagamentoPagoERegistrarTentativa() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);

        registrarCheckInBloqueado(alunoId, "Matricula nao possui pagamento pago");
    }

    @Test
    void deveBloquearMatriculaSemPagamentoPagoQuandoPagamentoEstaCanceladoERegistrarTentativa() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);
        Long pagamentoId = criarPagamento(matriculaId, "99.90", "PIX");
        cancelarPagamento(pagamentoId);

        registrarCheckInBloqueado(alunoId, "Matricula nao possui pagamento pago");
        assertThat(pagamentoRepository.findById(pagamentoId).orElseThrow().getStatus())
                .isEqualTo(StatusPagamento.CANCELADO);
    }

    @Test
    void deveRetornarErroDeValidacaoQuandoAlunoIdForNulo() throws Exception {
        mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alunoId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.path").value("/checkins"))
                .andExpect(jsonPath("$.errors.alunoId").value("Aluno e obrigatorio"));

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveRetornarErroAoBuscarCheckInInexistente() throws Exception {
        mockMvc.perform(get("/checkins/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Check-in nao encontrado com id: 999999"))
                .andExpect(jsonPath("$.path").value("/checkins/999999"));
    }

    private void registrarCheckInBloqueado(Long alunoId, String motivoEsperado) throws Exception {
        mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson(alunoId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.permitido").value(false))
                .andExpect(jsonPath("$.motivo").value(motivoEsperado))
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.aluno").doesNotExist())
                .andExpect(jsonPath("$.matricula").doesNotExist());

        assertThat(checkInRepository.count()).isEqualTo(1);
        CheckIn checkIn = checkInRepository.findAll().getFirst();
        assertThat(checkIn.getAluno().getId()).isEqualTo(alunoId);
        assertThat(checkIn.getPermitido()).isFalse();
        assertThat(checkIn.getMatricula()).isNull();
        assertThat(checkIn.getMotivo()).isEqualTo(motivoEsperado);
    }

    private Long registrarCheckIn(Long alunoId) throws Exception {
        MvcResult result = mockMvc.perform(post("/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson(alunoId)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarMatriculaValida(String alunoNome, String alunoEmail, String planoNome) throws Exception {
        Long alunoId = criarAluno(alunoNome, alunoEmail, "11999999999");
        Long planoId = criarPlano(planoNome, "Acesso por 30 dias", "99.90", 30);
        return criarMatricula(alunoId, planoId, LocalDate.now());
    }

    private Long criarMatriculaVencida() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        return criarMatricula(alunoId, planoId, LocalDate.now().minusDays(60));
    }

    private Long criarAlunoComStatus(StatusAluno status) throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow();
        aluno.setStatus(status);
        alunoRepository.save(aluno);
        return alunoId;
    }

    private Long buscarAlunoIdPorMatricula(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow();
        return matricula.getAluno().getId();
    }

    private Long criarAluno(String nome, String email, String telefone) throws Exception {
        MvcResult result = mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson(nome, email, telefone)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarPlano(String nome, String descricao, String valorMensal, int duracaoEmDias) throws Exception {
        MvcResult result = mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson(nome, descricao, valorMensal, duracaoEmDias)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarMatricula(Long alunoId, Long planoId, LocalDate dataInicio) throws Exception {
        MvcResult result = mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, planoId, dataInicio)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarPagamento(Long matriculaId, String valor, String formaPagamento) throws Exception {
        MvcResult result = mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, valor, formaPagamento)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private void cancelarPagamento(Long pagamentoId) throws Exception {
        mockMvc.perform(patch("/pagamentos/{id}/cancelar", pagamentoId))
                .andExpect(status().isOk());
    }

    private String checkInJson(Long alunoId) {
        return """
                {
                  "alunoId": %d
                }
                """.formatted(alunoId);
    }

    private String alunoJson(String nome, String email, String telefone) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "telefone": "%s"
                }
                """.formatted(nome, email, telefone);
    }

    private String planoJson(String nome, String descricao, String valorMensal, int duracaoEmDias) {
        return """
                {
                  "nome": "%s",
                  "descricao": "%s",
                  "valorMensal": %s,
                  "duracaoEmDias": %d
                }
                """.formatted(nome, descricao, valorMensal, duracaoEmDias);
    }

    private String matriculaJson(Long alunoId, Long planoId, LocalDate dataInicio) {
        return """
                {
                  "alunoId": %d,
                  "planoId": %d,
                  "dataInicio": "%s"
                }
                """.formatted(alunoId, planoId, dataInicio);
    }

    private String pagamentoJson(Long matriculaId, String valor, String formaPagamento) {
        return """
                {
                  "matriculaId": %d,
                  "valor": %s,
                  "formaPagamento": "%s"
                }
                """.formatted(matriculaId, valor, formaPagamento);
    }
}
