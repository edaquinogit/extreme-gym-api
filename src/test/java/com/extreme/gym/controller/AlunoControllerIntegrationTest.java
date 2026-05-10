package com.extreme.gym.controller;

import com.extreme.gym.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @BeforeEach
    void setUp() {
        alunoRepository.deleteAll();
    }

    @Test
    void deveCriarAlunoValido() throws Exception {
        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson("Ana Silva", "ana.silva@email.com", "11999999999")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@email.com"))
                .andExpect(jsonPath("$.telefone").value("11999999999"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.dataCadastro").exists());
    }

    @Test
    void deveListarAlunos() throws Exception {
        criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        criarAluno("Bruno Souza", "bruno.souza@email.com", "11888888888");

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                        "ana.silva@email.com",
                        "bruno.souza@email.com"
                )));
    }

    @Test
    void deveBuscarAlunoExistentePorId() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(get("/alunos/{id}", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alunoId))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@email.com"))
                .andExpect(jsonPath("$.telefone").value("11999999999"));
    }

    @Test
    void deveRetornarErroDeValidacaoQuandoEmailForInvalido() throws Exception {
        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson("Ana Silva", "email-invalido", "11999999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.path").value("/alunos"))
                .andExpect(jsonPath("$.errors.email").value("Email deve ser valido"));
    }

    @Test
    void deveRetornarErroQuandoEmailForDuplicadoNoCadastro() throws Exception {
        criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson("Ana Souza", "ana.silva@email.com", "11888888888")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ja existe aluno cadastrado com este email"))
                .andExpect(jsonPath("$.path").value("/alunos"));
    }

    @Test
    void deveAtualizarAlunoMantendoProprioEmail() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");

        mockMvc.perform(put("/alunos/{id}", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson("Ana Silva Atualizada", "ana.silva@email.com", "11777777777")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alunoId))
                .andExpect(jsonPath("$.nome").value("Ana Silva Atualizada"))
                .andExpect(jsonPath("$.email").value("ana.silva@email.com"))
                .andExpect(jsonPath("$.telefone").value("11777777777"));
    }

    @Test
    void deveBloquearAtualizacaoComEmailUsadoPorOutroAluno() throws Exception {
        Long alunoId = criarAluno("Ana Silva", "ana.silva@email.com", "11999999999");
        criarAluno("Bruno Souza", "bruno.souza@email.com", "11888888888");

        mockMvc.perform(put("/alunos/{id}", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alunoJson("Ana Silva", "bruno.souza@email.com", "11999999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ja existe aluno cadastrado com este email"))
                .andExpect(jsonPath("$.path").value("/alunos/" + alunoId));
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

    private String alunoJson(String nome, String email, String telefone) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "telefone": "%s"
                }
                """.formatted(nome, email, telefone);
    }
}
