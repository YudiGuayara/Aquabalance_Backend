package com.AquaBalance.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Autentica a los sensores/estaciones reales con la cabecera X-API-Key.
 * La clave SOLO vale para POST /api/estaciones/{id}/telemetria y /perdida;
 * en cualquier otra ruta no concede nada. No es un @Component: SecurityConfig la
 * registra una sola vez en la cadena de seguridad.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";
    public static final String ROL_SENSOR = "ROLE_SENSOR";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final Pattern RUTA_SENSOR =
            Pattern.compile("^/api/estaciones/\\d+/(telemetria|perdida)$");

    private final byte[] claveEsperada;

    public ApiKeyAuthenticationFilter(String claveEsperada) {
        this.claveEsperada = claveEsperada == null
                ? new byte[0] : claveEsperada.trim().getBytes(StandardCharsets.UTF_8);
        if (this.claveEsperada.length == 0) {
            log.info("sensores.api-key vacia: el acceso de sensores por X-API-Key esta deshabilitado.");
        }
    }

    public static boolean esRutaSensor(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && request.getServletPath() != null
                && RUTA_SENSOR.matcher(request.getServletPath()).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clave = request.getHeader(HEADER);

        // Sin cabecera, o ruta que no es de sensores: este filtro no interviene.
        if (clave == null || !esRutaSensor(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!claveValida(clave)) {
            log.warn("Clave de sensor invalida desde {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "sensor", null, List.of(new SimpleGrantedAuthority(ROL_SENSOR))));
        filterChain.doFilter(request, response);
    }

    /** Comparacion en tiempo constante para no filtrar la clave por tiempos de respuesta. */
    private boolean claveValida(String recibida) {
        if (claveEsperada.length == 0) return false;
        return MessageDigest.isEqual(claveEsperada, recibida.trim().getBytes(StandardCharsets.UTF_8));
    }
}
