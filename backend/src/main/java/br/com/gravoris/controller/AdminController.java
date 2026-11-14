package br.com.gravoris.controller;

import br.com.gravoris.dto.*;
import br.com.gravoris.security.CurrentUser;
import br.com.gravoris.service.AdminUsuarioService;
import br.com.gravoris.service.AdminVeiculoService;
import br.com.gravoris.service.ClienteService;
import br.com.gravoris.service.DashboardService;
import br.com.gravoris.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoints do painel administrativo.
 * Tudo em /api/admin/** exige papel ADMINISTRADOR (SecurityConfig), exceto o login.
 */
@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    private final ClienteService clienteService;
    private final DashboardService dashboardService;
    private final AdminVeiculoService veiculoService;
    private final AdminUsuarioService usuarioService;
    private final StorageService storageService;

    public AdminController(ClienteService clienteService, DashboardService dashboardService,
                           AdminVeiculoService veiculoService, AdminUsuarioService usuarioService,
                           StorageService storageService) {
        this.clienteService = clienteService;
        this.dashboardService = dashboardService;
        this.veiculoService = veiculoService;
        this.usuarioService = usuarioService;
        this.storageService = storageService;
    }

    // ---------- Autenticação ----------

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(clienteService.loginAdmin(request));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<AdminUsuarioResponse> me() {
        return ResponseEntity.ok(AdminUsuarioService.toResponse(clienteService.buscarPorId(CurrentUser.get().id())));
    }

    // ---------- Dashboard ----------

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.resumo());
    }

    // ---------- Veículos / anúncios ----------

    @GetMapping("/veiculos")
    public ResponseEntity<List<AdminProdutoResponse>> listarVeiculos(
            @RequestParam(name = "q", required = false) String termo,
            @RequestParam(name = "status", required = false) String status) {
        return ResponseEntity.ok(veiculoService.listar(termo, status));
    }

    @GetMapping("/veiculos/{id}")
    public ResponseEntity<AdminProdutoResponse> buscarVeiculo(@PathVariable Long id) {
        return ResponseEntity.ok(veiculoService.buscar(id));
    }

    @PostMapping(value = "/veiculos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminProdutoResponse> criarVeiculo(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(201).body(veiculoService.criar(request));
    }

    @PutMapping(value = "/veiculos/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminProdutoResponse> atualizarVeiculo(@PathVariable Long id,
                                                                 @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(veiculoService.atualizar(id, request));
    }

    @PatchMapping(value = "/veiculos/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminProdutoResponse> alterarStatusVeiculo(@PathVariable Long id,
                                                                     @Valid @RequestBody StatusAnuncioRequest request) {
        return ResponseEntity.ok(veiculoService.alterarStatus(id, request.status()));
    }

    @DeleteMapping("/veiculos/{id}")
    public ResponseEntity<Void> excluirVeiculo(@PathVariable Long id) {
        veiculoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/uploads/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> enviarImagem(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(new UploadResponse(storageService.enviarImagem(file)));
    }

    @GetMapping("/uploads/status")
    public ResponseEntity<java.util.Map<String, Boolean>> statusUpload() {
        return ResponseEntity.ok(java.util.Map.of("configurado", storageService.configurado()));
    }

    // ---------- Usuários ----------

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioResponse>> listarUsuarios(
            @RequestParam(name = "q", required = false) String termo) {
        return ResponseEntity.ok(usuarioService.listar(termo));
    }

    @PatchMapping(value = "/usuarios/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminUsuarioResponse> alterarStatusUsuario(@PathVariable Long id,
                                                                     @Valid @RequestBody UsuarioStatusRequest request) {
        return ResponseEntity.ok(usuarioService.alterarStatus(id, request.ativo(), CurrentUser.get().id()));
    }

    @PatchMapping(value = "/usuarios/{id}/role", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminUsuarioResponse> alterarRoleUsuario(@PathVariable Long id,
                                                                   @Valid @RequestBody UsuarioRoleRequest request) {
        return ResponseEntity.ok(usuarioService.alterarRole(id, request.role(), CurrentUser.get().id()));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        usuarioService.excluir(id, CurrentUser.get().id());
        return ResponseEntity.noContent().build();
    }
}
