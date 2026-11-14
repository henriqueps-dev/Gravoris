package br.com.gravoris.dto;

import jakarta.validation.constraints.NotBlank;

/** Novo papel do usuário: CLIENTE ou ADMINISTRADOR. */
public record UsuarioRoleRequest(@NotBlank(message = "Papel é obrigatório") String role) {
}
