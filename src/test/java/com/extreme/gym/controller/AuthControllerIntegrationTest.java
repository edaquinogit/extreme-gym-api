package com.extreme.gym.controller;

import com.extreme.gym.entity.Usuario;
import com.extreme.gym.enums.Role;
import com.extreme.gym.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.security.enabled=true",
        "app.auth.registration-enabled=true"
})
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveAutenticarUsuarioValidoERetornarToken() throws Exception {
        criarUsuario("Admin", "admin@email.com", "123456", Role.ADMIN);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@email.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600))
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void naoDeveExporContratoDeLoginViaGet() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deveRejeitarLoginComSenhaInvalida() throws Exception {
        criarUsuario("Admin", "admin@email.com", "123456", Role.ADMIN);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@email.com", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"));
    }

    @Test
    void deveBloquearEndpointProtegidoSemToken() throws Exception {
        mockMvc.perform(get("/alunos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoAEndpointProtegidoComTokenValido() throws Exception {
        criarUsuario("Recepcao", "recepcao@email.com", "123456", Role.RECEPCAO);
        String token = autenticar("recepcao@email.com", "123456");

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarAcessoQuandoUsuarioNaoPossuiRoleNecessaria() throws Exception {
        criarUsuario("Catraca", "catraca@email.com", "123456", Role.CATRACA);
        String token = autenticar("catraca@email.com", "123456");

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRegistrarUsuarioComRolePadraoRecepcaoEEmailNormalizado() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Novo Usuario", "USER@email.com", "123456")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.role").value("RECEPCAO"));

        Usuario usuario = usuarioRepository.findByEmail("user@email.com").orElseThrow();
        assertEquals("user@email.com", usuario.getEmail());
        assertEquals(Role.RECEPCAO, usuario.getRole());
        assertTrue(passwordEncoder.matches("123456", usuario.getPasswordHash()));
    }

    @Test
    void deveRejeitarLoginDeUsuarioInativo() throws Exception {
        Usuario usuario = Usuario.builder()
                .nome("Inativo")
                .email("inativo@email.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .role(Role.RECEPCAO)
                .ativo(false)
                .build();
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("inativo@email.com", "123456")))
                .andExpect(status().isUnauthorized());
    }

    private void criarUsuario(String nome, String email, String senha, Role role) {
        usuarioRepository.save(Usuario.builder()
                .nome(nome)
                .email(email)
                .username(email)
                .passwordHash(passwordEncoder.encode(senha))
                .role(role)
                .ativo(true)
                .build());
    }

    private String autenticar(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, senha)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String loginJson(String email, String senha) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "login", email,
                "password", senha
        ));
    }

    private String registerJson(String nome, String email, String senha) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "nome", nome,
                "email", email,
                "senha", senha
        ));
    }
}
