package com.extreme.gym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusAluno;
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

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AcessoControllerIntegrationTest {

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
    void deveLiberarAcessoComAlunoAtivoMatriculaAtivaNaoVencidaEPagamentoPago() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);
        criarPagamento(matriculaId, "99.90", "PIX");

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(true))
                .andExpect(jsonPath("$.motivo").value("Acesso liberado"))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.dataValidadeMatricula").exists());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveRetornarErroQuandoAlunoForInexistente() throws Exception {
        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Aluno nao encontrado com id: 999999"))
                .andExpect(jsonPath("$.path").value("/acessos/validar"));

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoBloqueado() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.BLOQUEADO);

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Aluno bloqueado"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.dataValidadeMatricula").doesNotExist());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoCancelado() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.CANCELADO);

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Aluno cancelado"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.dataValidadeMatricula").doesNotExist());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoInadimplente() throws Exception {
        Long alunoId = criarAlunoComStatus(StatusAluno.INADIMPLENTE);

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Aluno inadimplente"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.dataValidadeMatricula").doesNotExist());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoSemMatriculaAtiva() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Aluno nao possui matricula ativa"))
                .andExpect(jsonPath("$.matriculaId").doesNotExist())
                .andExpect(jsonPath("$.dataValidadeMatricula").doesNotExist());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearMatriculaVencida() throws Exception {
        Long matriculaId = criarMatriculaVencida();
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Matricula vencida"))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.dataValidadeMatricula").exists());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveBloquearAlunoSemPagamentoPago() throws Exception {
        Long matriculaId = criarMatriculaValida("Ana Silva", "ana.silva@email.com", "Plano Mensal");
        Long alunoId = buscarAlunoIdPorMatricula(matriculaId);

        mockMvc.perform(post("/acessos/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acessoJson(alunoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.alunoNome").value("Ana Silva"))
                .andExpect(jsonPath("$.acessoLiberado").value(false))
                .andExpect(jsonPath("$.motivo").value("Matricula nao possui pagamento pago"))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId))
                .andExpect(jsonPath("$.dataValidadeMatricula").exists());

        assertThat(checkInRepository.count()).isZero();
    }

    @Test
    void deveRetornarErroDeValidacaoQuandoAlunoIdForNulo() throws Exception {
        mockMvc.perform(post("/acessos/validar")
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
                .andExpect(jsonPath("$.path").value("/acessos/validar"))
                .andExpect(jsonPath("$.errors.alunoId").value("Aluno e obrigatorio"));

        assertThat(checkInRepository.count()).isZero();
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

    private String acessoJson(Long alunoId) {
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
