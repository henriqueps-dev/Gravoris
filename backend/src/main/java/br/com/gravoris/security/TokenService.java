package br.com.gravoris.security;

import br.com.gravoris.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Emite os tokens JWT assinados com HS256.
 * O claim "ctx" separa sessões de cliente ("cliente") das sessões administrativas ("admin").
 */
@Service
public class TokenService {

    public static final String ISSUER = "gravoris";
    public static final String CLAIM_CONTEXT = "ctx";
    public static final String CONTEXT_ADMIN = "admin";
    public static final String CONTEXT_CLIENTE = "cliente";

    private final JwtEncoder encoder;
    private final long clienteMinutes;
    private final long adminMinutes;

    public TokenService(JwtEncoder encoder,
                        @Value("${app.security.jwt-expiration-minutes}") long clienteMinutes,
                        @Value("${app.security.admin-jwt-expiration-minutes}") long adminMinutes) {
        this.encoder = encoder;
        this.clienteMinutes = clienteMinutes;
        this.adminMinutes = adminMinutes;
    }

    public IssuedToken emitirTokenCliente(Cliente cliente) {
        return emitir(cliente, CONTEXT_CLIENTE, clienteMinutes);
    }

    public IssuedToken emitirTokenAdmin(Cliente admin) {
        return emitir(admin, CONTEXT_ADMIN, adminMinutes);
    }

    private IssuedToken emitir(Cliente cliente, String contexto, long minutos) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(minutos, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(cliente.getId()))
                .issuedAt(agora)
                .expiresAt(expira)
                .claim(CLAIM_CONTEXT, contexto)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(valor, expira);
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
