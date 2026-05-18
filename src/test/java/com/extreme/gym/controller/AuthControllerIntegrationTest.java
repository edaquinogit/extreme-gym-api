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
@SpringBootTest(properties = "app.security.enabled=true")
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
    void deveRegistrarUsuarioComSenhaCriptografada() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin", "admin@email.com", "123456", Role.ADMIN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.role").value("RECEPCAO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Usuario usuario = usuarioRepository.findByEmail("admin@email.com").orElseThrow();
        assertTrue(passwordEncoder.matches("123456", usuario.getPasswordHash()));
        assertEquals(Role.RECEPCAO, usuario.getRole());
    }

    @Test
    void naoDevePermitirCriarAdminViaRegistroPublico() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin Publico", "admin-publico@email.com", "123456", Role.ADMIN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("RECEPCAO"));

        Usuario usuario = usuarioRepository.findByEmail("admin-publico@email.com").orElseThrow();
        assertEquals(Role.RECEPCAO, usuario.getRole());
    }

    @Test
    void tokenValidoDeUsuarioAtivoDeveAutenticar() throws Exception {
        criarUsuario("Recepcao", "ativo@email.com", "123456", Role.RECEPCAO);
        String token = autenticar("ativo@email.com", "123456");

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tokenDeUsuarioInexistenteNaoDeveAutenticar() throws Exception {
        criarUsuario("Recepcao", "removido@email.com", "123456", Role.RECEPCAO);
        String token = autenticar("removido@email.com", "123456");
        usuarioRepository.deleteAll();

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioInativoNaoDeveAutenticar() throws Exception {
        criarUsuario("Recepcao", "inativo@email.com", "123456", Role.RECEPCAO);
        String token = autenticar("inativo@email.com", "123456");
        Usuario usuario = usuarioRepository.findByEmail("inativo@email.com").orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleAtualDoBancoDevePrevalecerSobreRoleAntigaDoToken() throws Exception {
        criarUsuario("Catraca", "role-alterada@email.com", "123456", Role.CATRACA);
        String token = autenticar("role-alterada@email.com", "123456");
        Usuario usuario = usuarioRepository.findByEmail("role-alterada@email.com").orElseThrow();
        usuario.setRole(Role.RECEPCAO);
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private void criarUsuario(String nome, String email, String senha, Role role) {
        usuarioRepository.save(Usuario.builder()
                .nome(nome)
                .email(email)
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
                "username", email,
                "password", senha
        ));
    }

    private String registerJson(String nome, String email, String senha, Role role) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "nome", nome,
                "email", email,
                "senha", senha,
                "role", role
        ));
    }
}
