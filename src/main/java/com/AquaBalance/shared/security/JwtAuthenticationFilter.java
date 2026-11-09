package com.AquaBalance.shared.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.AquaBalance.shared.security.JwtService;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        String path = request.getServletPath();

        // 🔥 RUTAS PUBLICAS
        // /ws: el navegador no puede enviar "Authorization" en el handshake de WebSocket/SockJS.
        // El token se valida en el frame CONNECT de STOMP (ver WebSocketAuthInterceptor).
        if (path.startsWith("/api/auth") || path.startsWith("/api/network") || path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Sensores con X-API-Key: los autentica ApiKeyAuthenticationFilter (que ya corrio antes).
        if (request.getHeader(ApiKeyAuthenticationFilter.HEADER) != null
                && ApiKeyAuthenticationFilter.esRutaSensor(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // ❌ SIN TOKEN = 401 (IMPORTANTE)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(jwt);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                if (!userDetails.isEnabled()) {
                    logger.warn("JWT rechazado en " + path + ": cuenta no habilitada (inactiva o correo sin verificar) -> " + email);
                } else if (!jwtService.isTokenValid(jwt, userDetails)) {
                    logger.warn("JWT rechazado en " + path + ": token vencido o no corresponde al usuario -> " + email);
                }

                if (userDetails.isEnabled() && jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(

                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            // Sin este log no hay forma de saber si fue firma invalida, token vencido o usuario inexistente
            logger.warn("JWT rechazado en " + path + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}