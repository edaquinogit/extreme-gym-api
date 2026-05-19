package com.extreme.gym.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "app.security.enabled=false",
        "jwt.secret=MzA3NDAzNTkwNzcwOTMzMDgyODI5NjM4NzYwNzA4Ng==",
        "spring.datasource.url=jdbc:h2:mem:security_disable_switch;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityDisableSwitchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void securityEnabledFalseForaDoProfileTestNaoDeveLiberarRotasProtegidas() throws Exception {
        mockMvc.perform(get("/alunos"))
                .andExpect(status().isUnauthorized());
    }
}
