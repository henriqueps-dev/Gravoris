package br.com.gravoris.security;

import br.com.gravoris.model.Cliente;
import br.com.gravoris.model.Role;
import br.com.gravoris.repository.ClienteRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte o JWT validado no usuário autenticado.
 *
 * <p>O papel NÃO é confiado ao token: ele é conferido no banco a cada requisição.
 * Assim, um usuário desativado ou um administrador rebaixado perde o acesso
 * imediatamente, mesmo com um token ainda dentro da validade.</p>
 */
@Component
public class JwtUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final ClienteRepository clienteRepository;

    public JwtUserConverter(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long id;
        try {
            id = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Token inválido");
        }

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));
        if (!cliente.isAtivo()) {
            throw new DisabledException("Usuário desativado");
        }

        boolean contextoAdmin = TokenService.CONTEXT_ADMIN.equals(jwt.getClaimAsString(TokenService.CLAIM_CONTEXT));
        if (contextoAdmin && !cliente.isAdministrador()) {
            throw new BadCredentialsException("Permissão de administrador revogada");
        }

        Role papelEfetivo = contextoAdmin ? Role.ADMINISTRADOR : Role.CLIENTE;
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + Role.CLIENTE.name()));
        if (contextoAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Role.ADMINISTRADOR.name()));
        }

        AuthenticatedUser principal = new AuthenticatedUser(cliente.getId(), cliente.getEmail(), papelEfetivo);
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }
}
