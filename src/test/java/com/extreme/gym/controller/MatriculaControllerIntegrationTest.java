package com.extreme.gym.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class MatriculaControllerIntegrationTest {

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
    void deveCriarMatriculaValida() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        LocalDate dataInicio = LocalDate.of(2026, 5, 10);

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, planoId, dataInicio)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.planoId").value(planoId))
                .andExpect(jsonPath("$.planoNome").value("Plano Mensal"))
                .andExpect(jsonPath("$.dataInicio").value("2026-05-10"))
                .andExpect(jsonPath("$.dataFim").value("2026-06-09"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.dataCadastro").exists());
    }

    @Test
    void deveListarMatriculas() throws Exception {
        Long alunoAnaId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long alunoBrunoId = criarAluno("Bruno Souza", "bruno.souza@email.com", "11888888888");
        Long planoMensalId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        Long planoTrimestralId = criarPlano("Plano Trimestral", "Acesso por 90 dias", "249.90", 90);

        criarMatricula(alunoAnaId, planoMensalId, LocalDate.of(2026, 5, 10));
        criarMatricula(alunoBrunoId, planoTrimestralId, LocalDate.of(2026, 5, 11));

        mockMvc.perform(get("/matriculas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].alunoNome", containsInAnyOrder(
                        "Ana Silva",
                        "Bruno Souza"
                )))
                .andExpect(jsonPath("$[*].planoNome", containsInAnyOrder(
                        "Plano Mensal",
                        "Plano Trimestral"
                )));
    }

    @Test
    void deveBuscarMatriculaExistentePorId() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        Long matriculaId = criarMatricula(alunoId, planoId, LocalDate.of(2026, 5, 10));

        mockMvc.perform(get("/matriculas/{id}", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId))
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.planoId").value(planoId))
                .andExpect(jsonPath("$.planoNome").value("Plano Mensal"))
                .andExpect(jsonPath("$.dataInicio").value("2026-05-10"))
                .andExpect(jsonPath("$.dataFim").value("2026-06-09"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void deveRetornarErroQuandoAlunoForInexistente() throws Exception {
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(999L, planoId, LocalDate.of(2026, 5, 10))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Aluno nao encontrado com id: 999"))
                .andExpect(jsonPath("$.path").value("/matriculas"));
    }

    @Test
    void deveRetornarErroQuandoPlanoForInexistente() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, 999L, LocalDate.of(2026, 5, 10))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Plano nao encontrado com id: 999"))
                .andExpect(jsonPath("$.path").value("/matriculas"));
    }

    @Test
    void deveRetornarErroQuandoPlanoForInativo() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        inativarPlano(planoId);

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, planoId, LocalDate.of(2026, 5, 10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Plano inativo nao pode ser usado em matricula"))
                .andExpect(jsonPath("$.path").value("/matriculas"));
    }

    @Test
    void deveBloquearAlunoComMatriculaAtivaExistente() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoMensalId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        Long planoTrimestralId = criarPlano("Plano Trimestral", "Acesso por 90 dias", "249.90", 90);
        criarMatricula(alunoId, planoMensalId, LocalDate.of(2026, 5, 10));

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaJson(alunoId, planoTrimestralId, LocalDate.of(2026, 6, 10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Aluno ja possui matricula ativa"))
                .andExpect(jsonPath("$.path").value("/matriculas"));
    }

    @Test
    void deveCancelarMatriculaSemExcluirFisicamente() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        Long matriculaId = criarMatricula(alunoId, planoId, LocalDate.of(2026, 5, 10));

        mockMvc.perform(patch("/matriculas/{id}/cancelar", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId))
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(get("/matriculas/{id}", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId))
                .andExpect(jsonPath("$.status").value("CANCELADA"));
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

    private void inativarPlano(Long planoId) throws Exception {
        mockMvc.perform(delete("/planos/{id}", planoId))
                .andExpect(status().isNoContent());
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
}
