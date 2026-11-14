package br.com.gravoris.exception;

/**
 * Exceção para recursos externos não configurados ou indisponíveis (ex.: Supabase Storage).
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
