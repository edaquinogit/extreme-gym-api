package com.extreme.gym.controller;

import com.extreme.gym.repository.PlanoRepository;
import com.jayway.jsonpath.JsonPath;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PlanoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanoRepository planoRepository;

    @BeforeEach
    void setUp() {
        planoRepository.deleteAll();
    }

    @Test
    void deveCriarPlanoValido() throws Exception {
        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson("Plano Mensal", "Acesso completo por 30 dias", "99.90", 30)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Plano Mensal"))
                .andExpect(jsonPath("$.descricao").value("Acesso completo por 30 dias"))
                .andExpect(jsonPath("$.valorMensal").value(99.90))
                .andExpect(jsonPath("$.duracaoEmDias").value(30))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.dataCadastro").exists());
    }

    @Test
    void deveListarPlanos() throws Exception {
        criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);
        criarPlano("Plano Trimestral", "Acesso por 90 dias", "249.90", 90);

        mockMvc.perform(get("/planos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder(
                        "Plano Mensal",
                        "Plano Trimestral"
                )));
    }

    @Test
    void deveBuscarPlanoExistentePorId() throws Exception {
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);

        mockMvc.perform(get("/planos/{id}", planoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planoId))
                .andExpect(jsonPath("$.nome").value("Plano Mensal"))
                .andExpect(jsonPath("$.descricao").value("Acesso por 30 dias"))
                .andExpect(jsonPath("$.valorMensal").value(99.90))
                .andExpect(jsonPath("$.duracaoEmDias").value(30))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveRetornarErroQuandoNomeForDuplicadoNoCadastro() throws Exception {
        criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);

        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson("Plano Mensal", "Outro plano mensal", "109.90", 30)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ja existe plano cadastrado com este nome"))
                .andExpect(jsonPath("$.path").value("/planos"));
    }

    @Test
    void deveRetornarErroDeValidacaoQuandoValorMensalForInvalido() throws Exception {
        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson("Plano Mensal", "Acesso por 30 dias", "0.00", 30)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.path").value("/planos"))
                .andExpect(jsonPath("$.errors.valorMensal").value("Valor mensal deve ser maior que zero"));
    }

    @Test
    void deveRetornarErroDeValidacaoQuandoDuracaoEmDiasForInvalida() throws Exception {
        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson("Plano Mensal", "Acesso por 30 dias", "99.90", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.path").value("/planos"))
                .andExpect(jsonPath("$.errors.duracaoEmDias").value("Duracao em dias deve ser maior que zero"));
    }

    @Test
    void deveAtualizarPlanoExistente() throws Exception {
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);

        mockMvc.perform(put("/planos/{id}", planoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planoJson("Plano Mensal Plus", "Acesso completo atualizado", "119.90", 45)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planoId))
                .andExpect(jsonPath("$.nome").value("Plano Mensal Plus"))
                .andExpect(jsonPath("$.descricao").value("Acesso completo atualizado"))
                .andExpect(jsonPath("$.valorMensal").value(119.90))
                .andExpect(jsonPath("$.duracaoEmDias").value(45))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveInativarPlanoAoRemover() throws Exception {
        Long planoId = criarPlano("Plano Mensal", "Acesso por 30 dias", "99.90", 30);

        mockMvc.perform(delete("/planos/{id}", planoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/planos/{id}", planoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planoId))
                .andExpect(jsonPath("$.ativo").value(false));
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
}
