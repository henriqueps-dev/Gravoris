package br.com.gravoris.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Dados de criação/edição de um veículo pelo painel administrativo.
 */
public record ProdutoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 255) String name,
        @NotBlank(message = "Marca é obrigatória") @Size(max = 255) String brand,
        @NotBlank(message = "Modelo é obrigatório") @Size(max = 255) String model,
        @NotNull(message = "Preço é obrigatório") @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "Preço inválido") BigDecimal price,
        @Size(max = 1000, message = "URL da imagem muito longa") String imageUrl,
        @Size(max = 5000, message = "Descrição muito longa") String description,
        @NotNull(message = "Ano é obrigatório") @Min(value = 1886, message = "Ano inválido") @Max(value = 2100, message = "Ano inválido") Integer year,
        @PositiveOrZero(message = "Quilometragem inválida") Integer mileage,
        @NotBlank(message = "Categoria é obrigatória") String category,
        @PositiveOrZero(message = "Estoque inválido") Integer stock,
        @Size(max = 255) String engine,
        @PositiveOrZero(message = "Potência inválida") Integer power,
        @Size(max = 255) String torque,
        @PositiveOrZero(message = "Velocidade inválida") Integer topSpeed,
        @Size(max = 255) String acceleration,
        String status
) {
}
