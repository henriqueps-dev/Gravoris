package br.com.gravoris.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Respostas 401/403 no mesmo formato JSON do GlobalExceptionHandler.
 */
public final class JsonSecurityHandlers {

    private JsonSecurityHandlers() {
    }

    public static AuthenticationEntryPoint unauthorized() {
        return (request, response, ex) -> write(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Sessão inválida ou expirada. Faça login novamente.");
    }

    public static AccessDeniedHandler forbidden() {
        return (request, response, ex) -> write(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
    }

    private static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\",\"status\":" + status
                + ",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
