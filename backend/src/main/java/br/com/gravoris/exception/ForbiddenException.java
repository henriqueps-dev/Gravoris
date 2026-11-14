package br.com.gravoris.exception;

/**
 * Exceção para usuário autenticado sem permissão sobre o recurso.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
