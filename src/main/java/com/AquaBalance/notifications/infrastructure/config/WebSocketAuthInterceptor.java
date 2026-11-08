package com.AquaBalance.notifications.infrastructure.config;

import com.AquaBalance.shared.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

/**
 * Autentica el frame CONNECT de STOMP con el mismo JWT que usa la API REST.
 *
 * El handshake de /ws es público (el navegador no puede mandar "Authorization" en un
 * WebSocket/SockJS), así que sin esto cualquiera podría suscribirse a /topic/notificaciones.
 * Solo pasan los roles que también pueden leer /api/notificaciones (ver SecurityConfig).
 */
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Set<String> ROLES_PERMITIDOS = Set.of("ROLE_Administrador", "ROLE_Operador");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessageDeliveryException("WebSocket: falta el token de autenticación");
        }

        String jwt = authHeader.substring(7);
        try {
            String email = jwtService.extractUsername(jwt);
            UserDetails userDetails = (email == null) ? null : userDetailsService.loadUserByUsername(email);

            if (userDetails == null || !userDetails.isEnabled() || !jwtService.isTokenValid(jwt, userDetails)) {
                throw new MessageDeliveryException("WebSocket: token inválido");
            }

            boolean rolPermitido = userDetails.getAuthorities().stream()
                    .anyMatch(a -> ROLES_PERMITIDOS.contains(a.getAuthority()));
            if (!rolPermitido) {
                throw new MessageDeliveryException("WebSocket: tu rol no puede recibir notificaciones");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()));
        } catch (MessageDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageDeliveryException("WebSocket: token inválido");
        }

        return message;
    }
}
