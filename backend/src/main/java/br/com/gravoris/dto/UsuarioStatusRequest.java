package br.com.gravoris.dto;

import jakarta.validation.constraints.NotNull;

/** Ativa ou desativa a conta de um usuário. */
public record UsuarioStatusRequest(@NotNull(message = "Informe se o usuário está ativo") Boolean ativo) {
}
