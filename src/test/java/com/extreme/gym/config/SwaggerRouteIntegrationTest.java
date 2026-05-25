package com.extreme.gym.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true",
        "app.security.enabled=false"
})
@AutoConfigureMockMvc
class SwaggerRouteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporContratoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header(HttpHeaders.ACCEPT, "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"openapi\"")))
                .andExpect(content().string(containsString("\"Extreme Gym API\"")));
    }

    @Test
    void deveRedirecionarSwaggerUiHtmlParaInterface() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")
                        .header(HttpHeaders.ACCEPT, "text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/swagger-ui/index.html"));
    }

    @Test
    void deveServirInterfaceSwaggerSemCairNoFallbackDaSpa() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")
                        .header(HttpHeaders.ACCEPT, "text/html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
