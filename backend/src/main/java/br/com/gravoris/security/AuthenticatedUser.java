package br.com.gravoris.security;

import br.com.gravoris.model.Role;

/**
 * Usuário autenticado na requisição atual.
 * {@code role} é o papel efetivo do token: ADMINISTRADOR apenas quando o login
 * foi feito pelo fluxo administrativo e o usuário continua administrador no banco.
 */
public record AuthenticatedUser(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMINISTRADOR;
    }
}
