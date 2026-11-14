package br.com.gravoris.controller;

import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import br.com.gravoris.repository.ClienteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Painel administrativo e RBAC: criação do primeiro administrador, separação entre
 * sessão de cliente e de administrador, e as operações de gestão.
 */
@SpringBootTest(properties = {
        "app.admin.bootstrap.email=admin.teste@gravoris.com",
        "app.admin.bootstrap.password=SenhaAdmin#2026",
        "app.admin.bootstrap.name=Admin Teste",
        "app.admin.bootstrap.cpf=98765432100"
})
@ActiveProfiles("dev")
class AdminIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.teste@gravoris.com";
    private static final String ADMIN_SENHA = "SenhaAdmin#2026";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ClienteRepository clienteRepository;

    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private JsonNode postJson(String url, Object body, String bearer, int esperado) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        if (bearer != null) req.header("Authorization", bearer);
        String resp = mvc.perform(req).andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString();
        return resp.isBlank() ? null : json.readTree(resp);
    }

    private String tokenAdmin() throws Exception {
        return "Bearer " + postJson("/api/admin/auth/login", Map.of("email", ADMIN_EMAIL, "password", ADMIN_SENHA), null, 200)
                .get("token").asText();
    }

    private Map<String, Object> novoCliente(String sufixo) {
        long n = System.nanoTime() % 100000000000L;
        return Map.of("name", "Cliente " + sufixo, "email", "cli" + sufixo + n + "@gravoris.com",
                "password", "Senha123!", "phone", "11999990000", "cpf", String.format("%011d", n),
                "role", "ADMINISTRADOR");
    }

    @Test
    @DisplayName("Primeiro administrador é criado pelo bootstrap e entra pelo login administrativo")
    void bootstrapELoginAdmin() throws Exception {
        Cliente admin = clienteRepository.findByEmailIgnoreCase(ADMIN_EMAIL).orElseThrow();
        assertEquals(Role.ADMINISTRADOR, admin.getRole());
        assertNotEquals(ADMIN_SENHA, admin.getSenha());

        JsonNode login = postJson("/api/admin/auth/login", Map.of("email", ADMIN_EMAIL, "password", ADMIN_SENHA), null, 200);
        assertEquals("ADMINISTRADOR", login.get("role").asText());

        postJson("/api/admin/auth/login", Map.of("email", ADMIN_EMAIL, "password", "errada"), null, 401);

        mvc.perform(get("/api/admin/auth/me").header("Authorization", "Bearer " + login.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL));
    }

    @Test
    @DisplayName("Cadastro público nunca cria administrador e clientes não acessam o painel")
    void clienteNaoAcessaPainel() throws Exception {
        Map<String, Object> dados = novoCliente("rbac");
        JsonNode criado = postJson("/api/clientes", dados, null, 200);
        Cliente salvo = clienteRepository.findById(criado.get("id").asLong()).orElseThrow();
        assertEquals(Role.CLIENTE, salvo.getRole(), "o campo role enviado no cadastro deve ser ignorado");

        // Sem token: 401
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());

        // Cliente comum não entra pelo login administrativo
        postJson("/api/admin/auth/login", Map.of("email", dados.get("email"), "password", "Senha123!"), null, 403);

        // Token de cliente não abre endpoints administrativos
        String tokenCliente = "Bearer " + postJson("/api/clientes/login",
                Map.of("email", dados.get("email"), "password", "Senha123!"), null, 200).get("token").asText();
        mvc.perform(get("/api/admin/dashboard").header("Authorization", tokenCliente)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/usuarios").header("Authorization", tokenCliente)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/admin/veiculos/1").header("Authorization", tokenCliente)).andExpect(status().isForbidden());

        // Mesmo um administrador, quando entra pelo login da loja, recebe sessão de cliente
        String tokenLoja = "Bearer " + postJson("/api/clientes/login",
                Map.of("email", ADMIN_EMAIL, "password", ADMIN_SENHA), null, 200).get("token").asText();
        mvc.perform(get("/api/admin/dashboard").header("Authorization", tokenLoja)).andExpect(status().isForbidden());

        // Recuperação de senha pública não funciona para contas de administrador
        postJson("/api/clientes/redefinir-senha",
                Map.of("email", ADMIN_EMAIL, "cpf", "98765432100", "newPassword", "Invasor123"), null, 400);
    }

    @Test
    @DisplayName("Dashboard e ciclo completo de um anúncio: criar, pendente, aprovar, editar, desativar e excluir")
    void gerenciarVeiculos() throws Exception {
        String admin = tokenAdmin();

        mvc.perform(get("/api/admin/dashboard").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVeiculos").value(greaterThan(0)))
                .andExpect(jsonPath("$.totalUsuarios").value(greaterThan(0)))
                .andExpect(jsonPath("$.totalAdministradores").value(greaterThanOrEqualTo(1)));

        Map<String, Object> veiculo = new java.util.HashMap<>(Map.of(
                "name", "Teste GT", "brand", "Gravoris", "model", "GT", "price", 1500000,
                "imageUrl", "https://example.com/gt.jpg", "year", 2025, "mileage", 0,
                "category", "ESPORTIVO", "stock", 3, "power", 700));
        veiculo.put("status", "PENDENTE");
        JsonNode criado = postJson("/api/admin/veiculos", veiculo, admin, 201);
        long id = criado.get("id").asLong();
        assertEquals("PENDENTE", criado.get("status").asText());

        // Pendente não aparece para o público
        mvc.perform(get("/api/produtos/{id}", id)).andExpect(status().isNotFound());

        // Aprovar publica o anúncio
        mvc.perform(patch("/api/admin/veiculos/{id}/status", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(get("/api/produtos/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Teste GT"));

        // Editar
        veiculo.put("name", "Teste GT Editado");
        veiculo.remove("status");
        mvc.perform(put("/api/admin/veiculos/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(veiculo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Teste GT Editado"))
                .andExpect(jsonPath("$.status").value("APROVADO"));

        // Validação no servidor
        veiculo.put("price", -10);
        mvc.perform(put("/api/admin/veiculos/{id}", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(veiculo)))
                .andExpect(status().isBadRequest());

        // Desativar tira da vitrine
        mvc.perform(patch("/api/admin/veiculos/{id}/status", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DESATIVADO\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/produtos/{id}", id)).andExpect(status().isNotFound());

        mvc.perform(get("/api/admin/veiculos").param("status", "DESATIVADO").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        // Excluir
        mvc.perform(delete("/api/admin/veiculos/{id}", id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/admin/veiculos/{id}", id).header("Authorization", admin)).andExpect(status().isNotFound());

        // Upload sem Supabase Storage configurado responde 503 com mensagem clara
        MockMultipartFile img = new MockMultipartFile("file", "a.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
        mvc.perform(multipart("/api/admin/uploads/imagem").file(img).header("Authorization", admin))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Veículo com pedidos não pode ser excluído, apenas desativado")
    void veiculoComPedidoNaoExclui() throws Exception {
        String admin = tokenAdmin();
        Map<String, Object> dados = novoCliente("compra");
        long clienteId = postJson("/api/clientes", dados, null, 200).get("id").asLong();
        String tokenCliente = "Bearer " + postJson("/api/clientes/login",
                Map.of("email", dados.get("email"), "password", "Senha123!"), null, 200).get("token").asText();

        JsonNode veiculo = postJson("/api/admin/veiculos", Map.of(
                "name", "Vendido", "brand", "Gravoris", "model", "V", "price", 900000, "year", 2024,
                "mileage", 10, "category", "SEDAN", "stock", 2, "power", 400), admin, 201);
        long produtoId = veiculo.get("id").asLong();

        postJson("/api/pedidos", Map.of("clienteId", clienteId,
                "items", java.util.List.of(Map.of("productId", produtoId, "quantity", 1, "price", 900000))), tokenCliente, 200);

        mvc.perform(delete("/api/admin/veiculos/{id}", produtoId).header("Authorization", admin))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/admin/usuarios/{id}", clienteId).header("Authorization", admin))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Gestão de usuários: desativar bloqueia o token na hora, promover e proteções do próprio admin")
    void gerenciarUsuarios() throws Exception {
        String admin = tokenAdmin();
        Long adminId = clienteRepository.findByEmailIgnoreCase(ADMIN_EMAIL).orElseThrow().getId();

        Map<String, Object> dados = novoCliente("gestao");
        long id = postJson("/api/clientes", dados, null, 200).get("id").asLong();
        String tokenCliente = "Bearer " + postJson("/api/clientes/login",
                Map.of("email", dados.get("email"), "password", "Senha123!"), null, 200).get("token").asText();

        mvc.perform(get("/api/admin/usuarios").param("q", (String) dados.get("email")).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("CLIENTE"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());

        // Desativar: o token que o cliente já tinha deixa de valer imediatamente
        mvc.perform(patch("/api/admin/usuarios/{id}/status", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(get("/api/clientes/{id}", id).header("Authorization", tokenCliente)).andExpect(status().isUnauthorized());
        postJson("/api/clientes/login", Map.of("email", dados.get("email"), "password", "Senha123!"), null, 403);

        mvc.perform(patch("/api/admin/usuarios/{id}/status", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":true}"))
                .andExpect(status().isOk());

        // Promover a administrador e depois rebaixar
        mvc.perform(patch("/api/admin/usuarios/{id}/role", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMINISTRADOR"));
        String tokenNovoAdmin = tokenAdminDe((String) dados.get("email"));
        mvc.perform(patch("/api/admin/usuarios/{id}/role", id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"CLIENTE\"}"))
                .andExpect(status().isOk());
        // O token administrativo de quem foi rebaixado perde o acesso
        mvc.perform(get("/api/admin/dashboard").header("Authorization", tokenNovoAdmin)).andExpect(status().isUnauthorized());

        // O administrador não pode se desativar, se rebaixar ou se excluir
        mvc.perform(patch("/api/admin/usuarios/{id}/status", adminId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/admin/usuarios/{id}/role", adminId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"CLIENTE\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/admin/usuarios/{id}", adminId).header("Authorization", admin))
                .andExpect(status().isBadRequest());

        // Usuário sem pedidos pode ser excluído
        mvc.perform(delete("/api/admin/usuarios/{id}", id).header("Authorization", admin)).andExpect(status().isNoContent());
        assertTrue(clienteRepository.findById(id).isEmpty());
    }

    private String tokenAdminDe(String email) throws Exception {
        return "Bearer " + postJson("/api/admin/auth/login", Map.of("email", email, "password", "Senha123!"), null, 200)
                .get("token").asText();
    }
}
