package br.com.gravoris.loader;

import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import br.com.gravoris.repository.ClienteRepository;
import br.com.gravoris.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o primeiro administrador a partir de variáveis de ambiente protegidas.
 *
 * <p>Só age quando ADMIN_BOOTSTRAP_EMAIL e ADMIN_BOOTSTRAP_PASSWORD estão definidas
 * e ainda NÃO existe nenhum administrador. Depois do primeiro uso, ele não faz mais nada,
 * então as variáveis podem (e devem) ser removidas do ambiente.</p>
 *
 * <p>Se o e-mail já pertence a um cliente cadastrado, essa conta é promovida a
 * administrador e a senha informada passa a valer.</p>
 */
@Component
@Order(1)
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final String email;
    private final String senha;
    private final String nome;
    private final String cpf;

    public AdminBootstrap(ClienteRepository clienteRepository, ClienteService clienteService,
                          @Value("${app.admin.bootstrap.email:}") String email,
                          @Value("${app.admin.bootstrap.password:}") String senha,
                          @Value("${app.admin.bootstrap.name:Administrador}") String nome,
                          @Value("${app.admin.bootstrap.cpf:}") String cpf) {
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.email = ClienteService.normalizarEmail(email);
        this.senha = senha;
        this.nome = nome;
        this.cpf = ClienteService.normalizarCpf(cpf);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (clienteRepository.existsByRole(Role.ADMINISTRADOR)) {
            return;
        }
        if (email.isBlank() || senha == null || senha.isBlank()) {
            log.warn("Nenhum administrador cadastrado. Defina ADMIN_BOOTSTRAP_EMAIL e "
                    + "ADMIN_BOOTSTRAP_PASSWORD para criar o primeiro administrador.");
            return;
        }
        if (senha.length() < 10) {
            log.error("ADMIN_BOOTSTRAP_PASSWORD precisa ter pelo menos 10 caracteres. Administrador não criado.");
            return;
        }

        Cliente admin = clienteRepository.findByEmailIgnoreCase(email).orElse(null);
        if (admin == null) {
            if (cpf.length() != 11) {
                log.error("ADMIN_BOOTSTRAP_CPF (11 dígitos) é obrigatório para criar o administrador. Administrador não criado.");
                return;
            }
            if (clienteRepository.findByCpf(cpf).isPresent()) {
                log.error("O CPF informado em ADMIN_BOOTSTRAP_CPF já pertence a outra conta. Administrador não criado.");
                return;
            }
            admin = Cliente.builder().nome(nome).email(email).cpf(cpf).ativo(true).build();
        }

        admin.setRole(Role.ADMINISTRADOR);
        admin.setAtivo(true);
        admin.setSenha(clienteService.hashSenha(senha));
        clienteRepository.save(admin);
        log.info("Primeiro administrador configurado para {}. Remova as variáveis ADMIN_BOOTSTRAP_* do ambiente.", email);
    }
}
