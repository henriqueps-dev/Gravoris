package br.com.gravoris.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload usado para redefinir a senha de um cliente.
 * O CPF é exigido junto com o e-mail para confirmar a identidade.
 */
public record RedefinirSenhaRequest(
        @NotBlank @Email String email,
        @NotBlank(message = "CPF é obrigatório") String cpf,
        @NotBlank @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String newPassword
) {
}
