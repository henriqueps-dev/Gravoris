package br.com.gravoris.controller;

import br.com.gravoris.dto.ClienteRequest;
import br.com.gravoris.dto.LoginRequest;
import br.com.gravoris.dto.RedefinirSenhaRequest;
import br.com.gravoris.model.Cliente;
import br.com.gravoris.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("dev")
public class ClienteControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ClienteRepository clienteRepository;

    private MockMvc mockMvc;
    private final List<Long> createdClienteIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdClienteIds) {
            try {
                clienteRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Fluxo completo do Cliente: cadastro, login, consulta por ID, atualização e redefinição de senha")
    void testClienteCompleteLifecycle() throws Exception {
        long timestamp = System.currentTimeMillis();
        String testEmail = "testuser" + timestamp + "@gravoris.com";
        String testCpf = String.format("%011d", (timestamp % 100000000000L));
        String initialPassword = "Password123!";
        String newPassword = "NewSecretPassword456!";

        // 1. Cadastrar cliente
        ClienteRequest cadastroReq = new ClienteRequest(
                "Piloto Teste",
                testEmail,
                initialPassword,
                "11987654321",
                testCpf
        );

        String cadastroJson = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Piloto Teste"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.phone").value("11987654321"))
                .andExpect(jsonPath("$.cpf").value(testCpf))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();

        Long clienteId = objectMapper.readTree(cadastroJson).get("id").asLong();
        createdClienteIds.add(clienteId);

        // Confirmar persistência no banco de dados
        assertTrue(clienteRepository.findById(clienteId).isPresent());
        Cliente persistido = clienteRepository.findById(clienteId).get();
        assertEquals("Piloto Teste", persistido.getNome());
        assertNotEquals(initialPassword, persistido.getSenha()); // Deve estar hasheada com BCrypt

        // 2. Tentar cadastrar duplicado (conflito de email ou CPF - HTTP 409)
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroReq)))
                .andExpect(status().isConflict());

        // 3. Fazer login com as credenciais cadastradas
        LoginRequest loginReq = new LoginRequest(testEmail, initialPassword);
        String loginJson = mockMvc.perform(post("/api/clientes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.name").value("Piloto Teste"))
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String bearer = "Bearer " + objectMapper.readTree(loginJson).get("token").asText();

        // 4.1 Sem token, os dados do cliente não podem ser lidos (HTTP 401)
        mockMvc.perform(get("/api/clientes/{id}", clienteId))
                .andExpect(status().isUnauthorized());

        // 4.2 Token forjado/inválido é rejeitado (HTTP 401)
        mockMvc.perform(get("/api/clientes/{id}", clienteId).header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());

        // 4.3 Um cliente não pode ler os dados de outro cliente (HTTP 403)
        Cliente outro = clienteRepository.save(Cliente.builder().nome("Outro").email("outro" + timestamp + "@gravoris.com")
                .senha("x").cpf(String.format("%011d", (timestamp + 7) % 100000000000L)).build());
        createdClienteIds.add(outro.getId());
        mockMvc.perform(get("/api/clientes/{id}", outro.getId()).header("Authorization", bearer))
                .andExpect(status().isForbidden());

        // 4. Testar login com senha errada (HTTP 401)
        LoginRequest loginInvalido = new LoginRequest(testEmail, "SenhaErrada999");
        mockMvc.perform(post("/api/clientes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isUnauthorized());

        // 5. Consultar os dados do cliente por ID
        mockMvc.perform(get("/api/clientes/{id}", clienteId).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.name").value("Piloto Teste"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.cpf").value(testCpf));

        // 6. Atualizar os dados do cliente SEM enviar senha
        ClienteRequest updateReq = new ClienteRequest(
                "Piloto Teste Atualizado",
                testEmail,
                null, // Sem senha na atualização de perfil
                "11911112222",
                testCpf
        );

        mockMvc.perform(put("/api/clientes/{id}", clienteId)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.name").value("Piloto Teste Atualizado"))
                .andExpect(jsonPath("$.phone").value("11911112222"));

        Cliente clienteAtualizado = clienteRepository.findById(clienteId).get();
        assertEquals("Piloto Teste Atualizado", clienteAtualizado.getNome());
        assertEquals("11911112222", clienteAtualizado.getTelefone());
        // Senha permanece a mesma
        assertEquals(persistido.getSenha(), clienteAtualizado.getSenha());

        // 7. Redefinir a senha
        // 7.1 E-mail correto com CPF errado NÃO redefine a senha
        RedefinirSenhaRequest resetErrado = new RedefinirSenhaRequest(testEmail, "00000000000", newPassword);
        mockMvc.perform(post("/api/clientes/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetErrado)))
                .andExpect(status().isBadRequest());

        RedefinirSenhaRequest resetReq = new RedefinirSenhaRequest(testEmail, testCpf, newPassword);
        mockMvc.perform(post("/api/clientes/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk());

        // 8. Verificar que a senha antiga NÃO funciona mais
        mockMvc.perform(post("/api/clientes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());

        // 9. Verificar que a nova senha funciona com sucesso
        LoginRequest novoLogin = new LoginRequest(testEmail, newPassword);
        mockMvc.perform(post("/api/clientes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
