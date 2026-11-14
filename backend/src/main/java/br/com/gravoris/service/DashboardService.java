package br.com.gravoris.service;

import br.com.gravoris.dto.DashboardResponse;
import br.com.gravoris.model.Role;
import br.com.gravoris.model.StatusAnuncio;
import br.com.gravoris.model.StatusPedido;
import br.com.gravoris.repository.ClienteRepository;
import br.com.gravoris.repository.PedidoRepository;
import br.com.gravoris.repository.ProdutoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Indicadores gerais da plataforma para o dashboard administrativo.
 */
@Service
public class DashboardService {

    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public DashboardService(ProdutoRepository produtoRepository, ClienteRepository clienteRepository,
                            PedidoRepository pedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo() {
        long totalVeiculos = produtoRepository.count();
        // ativo = true equivale a APROVADO (regra mantida na entidade Produto)
        long aprovados = produtoRepository.countByAtivoTrue();
        long pendentes = produtoRepository.countByStatus(StatusAnuncio.PENDENTE);
        long desativados = Math.max(0, totalVeiculos - aprovados - pendentes);

        List<DashboardResponse.PedidoResumo> recentes = pedidoRepository.findRecentes(PageRequest.of(0, 6)).stream()
                .map(p -> new DashboardResponse.PedidoResumo(
                        p.getId(),
                        p.getCliente().getNome(),
                        p.getTotal(),
                        p.getStatus().name(),
                        p.getDataPedido().toString(),
                        p.getItens().stream().mapToInt(i -> i.getQuantidade()).sum()))
                .toList();

        return new DashboardResponse(
                totalVeiculos,
                aprovados,
                pendentes,
                desativados,
                produtoRepository.somarEstoqueAtivo(),
                clienteRepository.count(),
                clienteRepository.countByAtivoTrue(),
                clienteRepository.countByRole(Role.ADMINISTRADOR),
                pedidoRepository.count(),
                pedidoRepository.somarTotalExceto(StatusPedido.CANCELADO),
                recentes);
    }
}
