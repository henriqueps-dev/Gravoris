package br.com.gravoris.dto;

import java.time.LocalDateTime;

/**
 * Usuário listado no painel administrativo (sem senha).
 */
public record AdminUsuarioResponse(
        Long id,
        String name,
        String email,
        String phone,
        String cpf,
        String role,
        boolean ativo,
        LocalDateTime criadoEm
) {
}
