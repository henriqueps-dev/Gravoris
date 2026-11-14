package br.com.gravoris.security;

import br.com.gravoris.exception.ForbiddenException;
import br.com.gravoris.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitário para obter o usuário autenticado e checar a posse de recursos.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new UnauthorizedException("Autenticação necessária");
    }

    /** Permite o acesso ao próprio recurso ou a um administrador. */
    public static AuthenticatedUser exigirDonoOuAdmin(Long donoId) {
        AuthenticatedUser user = get();
        if (!user.isAdmin() && !user.id().equals(donoId)) {
            throw new ForbiddenException("Acesso negado");
        }
        return user;
    }
}
