package br.com.gravoris.dto;

import java.math.BigDecimal;

/**
 * Resposta simples de pedido para o frontend.
 */
public record PedidoResponse(
        Long id,
        BigDecimal total,
        String status,
        String dataPedido
) {
}
