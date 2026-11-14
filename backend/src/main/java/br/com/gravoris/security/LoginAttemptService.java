package br.com.gravoris.security;

import br.com.gravoris.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra força bruta: após 5 falhas seguidas para o mesmo e-mail,
 * novas tentativas ficam bloqueadas por 15 minutos.
 * Fica em memória; com várias instâncias do backend, troque por um armazenamento compartilhado.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FALHAS = 5;
    private static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Tentativas> tentativas = new ConcurrentHashMap<>();

    public void verificarBloqueio(String email) {
        Tentativas t = tentativas.get(chave(email));
        if (t != null && t.bloqueadoAte != null && Instant.now().isBefore(t.bloqueadoAte)) {
            throw new TooManyRequestsException("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
    }

    public void registrarFalha(String email) {
        tentativas.compute(chave(email), (k, atual) -> {
            boolean expirado = atual != null && atual.bloqueadoAte != null && Instant.now().isAfter(atual.bloqueadoAte);
            Tentativas t = (atual == null || expirado) ? new Tentativas() : atual;
            t.falhas++;
            if (t.falhas >= MAX_FALHAS) {
                t.bloqueadoAte = Instant.now().plus(BLOQUEIO);
            }
            return t;
        });
    }

    public void registrarSucesso(String email) {
        tentativas.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Tentativas {
        int falhas;
        Instant bloqueadoAte;
    }
}
