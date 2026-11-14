package br.com.gravoris.dto;

import jakarta.validation.constraints.NotBlank;

/** Novo status de um anúncio: PENDENTE, APROVADO ou DESATIVADO. */
public record StatusAnuncioRequest(@NotBlank(message = "Status é obrigatório") String status) {
}
