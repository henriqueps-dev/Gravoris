package br.com.gravoris.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Números gerais da plataforma para o dashboard administrativo.
 */
public record DashboardResponse(
        long totalVeiculos,
        long veiculosAprovados,
        long veiculosPendentes,
        long veiculosDesativados,
        long estoqueDisponivel,
        long totalUsuarios,
        long usuariosAtivos,
        long totalAdministradores,
        long totalPedidos,
        BigDecimal receitaPedidos,
        List<PedidoResumo> pedidosRecentes
) {
    public record PedidoResumo(Long id, String cliente, BigDecimal total, String status, String dataPedido, int itens) {
    }
}
