package br.com.gravoris.service;

import br.com.gravoris.dto.ClienteRequest;
import br.com.gravoris.dto.LoginRequest;
import br.com.gravoris.dto.LoginResponse;
import br.com.gravoris.dto.RedefinirSenhaRequest;
import br.com.gravoris.exception.BadRequestException;
import br.com.gravoris.exception.ConflictException;
import br.com.gravoris.exception.ForbiddenException;
import br.com.gravoris.exception.ResourceNotFoundException;
import br.com.gravoris.exception.UnauthorizedException;
import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import br.com.gravoris.repository.ClienteRepository;
import br.com.gravoris.security.LoginAttemptService;
import br.com.gravoris.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Serviço responsável pelos casos de uso de cliente e autenticação.
 */
@Service
public class ClienteService {

    private static final String CREDENCIAIS_INVALIDAS = "Email ou senha incorretos";

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttempts;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                          TokenService tokenService, LoginAttemptService loginAttempts) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginAttempts = loginAttempts;
    }

    /** Cadastro público: sempre cria um CLIENTE, nunca um administrador. */
    @Transactional
    public Cliente salvar(ClienteRequest request) {
        validarSenha(request.password());
        String email = normalizarEmail(request.email());
        String cpf = normalizarCpf(request.cpf());

        clienteRepository.findByEmailIgnoreCase(email)
                .ifPresent(cliente -> {
                    throw new ConflictException("Email já cadastrado");
                });

        clienteRepository.findByCpf(cpf)
                .ifPresent(cliente -> {
                    throw new ConflictException("CPF já cadastrado");
                });

        Cliente cliente = Cliente.builder()
                .nome(request.name().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.password()))
                .telefone(request.phone())
                .cpf(cpf)
                .ativo(true)
                .role(Role.CLIENTE)
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente alterar(Long id, ClienteRequest request) {
        Cliente cliente = buscarPorId(id);
        String email = normalizarEmail(request.email());
        String cpf = normalizarCpf(request.cpf());

        if (!cliente.getEmail().equalsIgnoreCase(email)) {
            clienteRepository.findByEmailIgnoreCase(email)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ConflictException("Email já cadastrado");
                        }
                    });
        }

        if (!cliente.getCpf().equals(cpf)) {
            clienteRepository.findByCpf(cpf)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ConflictException("CPF já cadastrado");
                        }
                    });
        }

        cliente.setNome(request.name().trim());
        cliente.setEmail(email);
        cliente.setTelefone(request.phone());
        cliente.setCpf(cpf);
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    /** Login da loja: gera uma sessão de cliente (mesmo para contas administradoras). */
    public LoginResponse login(LoginRequest request) {
        Cliente cliente = autenticar(request);
        TokenService.IssuedToken token = tokenService.emitirTokenCliente(cliente);
        return new LoginResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(),
                token.value(), Role.CLIENTE.name(), token.expiresAt());
    }

    /** Login do painel: só aceita contas com papel ADMINISTRADOR. */
    public LoginResponse loginAdmin(LoginRequest request) {
        Cliente cliente = autenticar(request);
        if (!cliente.isAdministrador()) {
            throw new ForbiddenException("Acesso restrito a administradores");
        }
        TokenService.IssuedToken token = tokenService.emitirTokenAdmin(cliente);
        return new LoginResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(),
                token.value(), Role.ADMINISTRADOR.name(), token.expiresAt());
    }

    private Cliente autenticar(LoginRequest request) {
        String email = normalizarEmail(request.email());
        loginAttempts.verificarBloqueio(email);

        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email).orElse(null);
        if (cliente == null || !passwordEncoder.matches(request.password(), cliente.getSenha())) {
            loginAttempts.registrarFalha(email);
            throw new UnauthorizedException(CREDENCIAIS_INVALIDAS);
        }
        if (!cliente.isAtivo()) {
            throw new ForbiddenException("Conta desativada. Entre em contato com o suporte.");
        }
        loginAttempts.registrarSucesso(email);
        return cliente;
    }

    /**
     * Redefinição de senha sem login: exige e-mail e CPF do mesmo cadastro.
     * Contas de administrador não podem ser redefinidas por este fluxo público.
     */
    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        String email = normalizarEmail(request.email());
        String chaveLimite = "reset:" + email;
        loginAttempts.verificarBloqueio(chaveLimite);
        validarSenha(request.newPassword());

        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email)
                .filter(c -> c.getCpf().equals(normalizarCpf(request.cpf())))
                .filter(c -> !c.isAdministrador())
                .orElse(null);
        if (cliente == null) {
            loginAttempts.registrarFalha(chaveLimite);
            throw new BadRequestException("Os dados informados não conferem com nenhuma conta");
        }

        loginAttempts.registrarSucesso(chaveLimite);
        cliente.setSenha(passwordEncoder.encode(request.newPassword()));
        clienteRepository.save(cliente);
    }

    public String hashSenha(String senha) {
        validarSenha(senha);
        return passwordEncoder.encode(senha);
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new BadRequestException("Senha obrigatória");
        }
        if (senha.length() < 6) {
            throw new BadRequestException("A senha deve ter pelo menos 6 caracteres");
        }
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }
}
