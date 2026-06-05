package br.com.gravoris.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO usado para criar e atualizar clientes a partir do frontend.
 */
public record ClienteRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        String phone,
        @NotBlank String cpf
) {
}
