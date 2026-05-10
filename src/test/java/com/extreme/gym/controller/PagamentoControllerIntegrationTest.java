package com.extreme.gym.controller;

import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import com.extreme.gym.repository.PlanoRepository;
import com.jayway.jsonpath.JsonPath;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PagamentoControllerIntegrationTest {

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
    void deveRegistrarPagamentoValidoParaMatriculaExistente() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, "99.90", "PIX")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.planoNome").value("Plano Mensal"))
                .andExpect(jsonPath("$.valor").value(99.90))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.dataPagamento").exists())
                .andExpect(jsonPath("$.dataCadastro").exists());
    }

    @Test
    void deveListarPagamentos() throws Exception {
        Long matriculaAnaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long matriculaBrunoId = criarMatriculaValida("Bruno Souza", "bruno.souza@email.com", "Plano Trimestral");

        criarPagamento(matriculaAnaId, "99.90", "PIX");
        criarPagamento(matriculaBrunoId, "249.90", "CARTAO_CREDITO");

        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].matriculaId", containsInAnyOrder(
                        matriculaAnaId.intValue(),
                        matriculaBrunoId.intValue()
                )))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("PAGO", "PAGO")));
    }

    @Test
    void deveBuscarPagamentoExistentePorId() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long pagamentoId = criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(get("/pagamentos/{id}", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pagamentoId))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.valor").value(99.90))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                .andExpect(jsonPath("$.status").value("PAGO"));
    }

    @Test
    void deveListarPagamentosPorMatricula() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(get("/pagamentos/matricula/{matriculaId}", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId))
                .andExpect(jsonPath("$[0].status").value("PAGO"));
    }

    @Test
    void deveRetornarErroQuandoMatriculaForInexistente() throws Exception {
        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(999L, "99.90", "PIX")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Matricula nao encontrada com id: 999"))
                .andExpect(jsonPath("$.path").value("/pagamentos"));
    }

    @Test
    void deveRetornarErroQuandoMatriculaEstiverCancelada() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        cancelarMatricula(matriculaId);

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, "99.90", "PIX")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Matricula cancelada nao pode receber pagamento"))
                .andExpect(jsonPath("$.path").value("/pagamentos"));
    }

    @Test
    void deveRetornarErroQuandoMatriculaEstiverVencida() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        marcarMatriculaComoVencida(matriculaId);

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, "99.90", "PIX")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Matricula vencida nao pode receber pagamento nesta versao"))
                .andExpect(jsonPath("$.path").value("/pagamentos"));
    }

    @Test
    void deveBloquearPagamentoDuplicadoPagoParaMesmaMatricula() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, "99.90", "DINHEIRO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Matricula ja possui pagamento pago registrado"))
                .andExpect(jsonPath("$.path").value("/pagamentos"));
    }

    @Test
    void deveCancelarPagamentoSemExcluirFisicamente() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long pagamentoId = criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(patch("/pagamentos/{id}/cancelar", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pagamentoId))
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(get("/pagamentos/{id}", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pagamentoId))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    private Long criarMatriculaValida(String alunoNome, String alunoEmail, String planoNome) throws Exception {
        Long alunoId = criarAluno(alunoNome, alunoEmail, "11999999999");
        Long planoId = criarPlano(planoNome, "Acesso por 30 dias", "99.90", 30);
        return criarMatricula(alunoId, planoId, LocalDate.of(2026, 5, 10));
    }

    private Long criarAluno(String nome, String email, String telefone) throws Exception {
        MvcResult result = mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson(nome, email, telefone)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarPlano(String nome, String descricao, String valorMensal, int duracaoEmDias) throws Exception {
        MvcResult result = mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson(nome, descricao, valorMensal, duracaoEmDias)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarMatricula(Long alunoId, Long planoId, LocalDate dataInicio) throws Exception {
        MvcResult result = mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, planoId, dataInicio)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long criarPagamento(Long matriculaId, String valor, String formaPagamento) throws Exception {
        MvcResult result = mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamentoJson(matriculaId, valor, formaPagamento)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private void cancelarMatricula(Long matriculaId) throws Exception {
        mockMvc.perform(patch("/matriculas/{id}/cancelar", matriculaId))
                .andExpect(status().isOk());
    }

    private void marcarMatriculaComoVencida(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow();
        matricula.setStatus(StatusMatricula.VENCIDA);
        matriculaRepository.save(matricula);
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
