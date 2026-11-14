package br.com.gravoris.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Veículo/anúncio com os campos de gestão visíveis apenas para administradores.
 */
public record AdminProdutoResponse(
        Long id,
        String name,
        String brand,
        String model,
        BigDecimal price,
        String imageUrl,
        String description,
        int year,
        int mileage,
        String category,
        int stock,
        String engine,
        int power,
        String torque,
        int topSpeed,
        String acceleration,
        String status,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
