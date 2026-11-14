package br.com.gravoris.service;

import br.com.gravoris.dto.AdminUsuarioResponse;
import br.com.gravoris.exception.BadRequestException;
import br.com.gravoris.exception.ConflictException;
import br.com.gravoris.exception.ResourceNotFoundException;
import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import br.com.gravoris.repository.ClienteRepository;
import br.com.gravoris.repository.PedidoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Gestão de usuários pelo painel administrativo.
 * Regras de proteção: o administrador não pode desativar, rebaixar ou excluir a própria
 * conta, e a plataforma nunca fica sem pelo menos um administrador ativo.
 */
@Service
public class AdminUsuarioService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public AdminUsuarioService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> listar(String termo) {
        List<Cliente> clientes = (termo == null || termo.isBlank())
                ? clienteRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                : clienteRepository.searchAdmin(termo.trim());
        return clientes.stream().map(AdminUsuarioService::toResponse).toList();
    }

    @Transactional
    public AdminUsuarioResponse alterarStatus(Long id, boolean ativo, Long adminId) {
        Cliente cliente = encontrar(id);
        if (id.equals(adminId) && !ativo) {
            throw new BadRequestException("Você não pode desativar a sua própria conta");
        }
        if (!ativo && cliente.isAdministrador() && cliente.isAtivo()) {
            garantirOutroAdminAtivo();
        }
        cliente.setAtivo(ativo);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public AdminUsuarioResponse alterarRole(Long id, String role, Long adminId) {
        Role novo = parseRole(role);
        Cliente cliente = encontrar(id);
        if (id.equals(adminId) && novo != Role.ADMINISTRADOR) {
            throw new BadRequestException("Você não pode remover o seu próprio acesso de administrador");
        }
        if (cliente.isAdministrador() && novo != Role.ADMINISTRADOR && cliente.isAtivo()) {
            garantirOutroAdminAtivo();
        }
        cliente.setRole(novo);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id, Long adminId) {
        Cliente cliente = encontrar(id);
        if (id.equals(adminId)) {
            throw new BadRequestException("Você não pode excluir a sua própria conta");
        }
        if (cliente.isAdministrador() && cliente.isAtivo()) {
            garantirOutroAdminAtivo();
        }
        if (pedidoRepository.existsByClienteId(id)) {
            throw new ConflictException("Este usuário possui pedidos. Desative a conta em vez de excluí-la.");
        }
        clienteRepository.delete(cliente);
    }

    private void garantirOutroAdminAtivo() {
        if (clienteRepository.countByRoleAndAtivoTrue(Role.ADMINISTRADOR) <= 1) {
            throw new BadRequestException("A plataforma precisa de pelo menos um administrador ativo");
        }
    }

    private Cliente encontrar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private static Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Papel inválido. Use CLIENTE ou ADMINISTRADOR");
        }
    }

    public static AdminUsuarioResponse toResponse(Cliente c) {
        return new AdminUsuarioResponse(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCpf(),
                c.getRole().name(), c.isAtivo(), c.getCriadoEm());
    }
}
