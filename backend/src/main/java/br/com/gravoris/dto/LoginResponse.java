package br.com.gravoris.dto;

import java.time.Instant;

/**
 * Resposta enviada ao frontend após autenticação.
 * {@code token} é um JWT assinado; {@code role} é o papel efetivo da sessão.
 */
public record LoginResponse(
        Long id,
        String name,
        String email,
        String token,
        String role,
        Instant expiresAt
) {
}
