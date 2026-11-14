package br.com.gravoris.exception;

/**
 * Exceção para excesso de tentativas (ex.: login por força bruta).
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
