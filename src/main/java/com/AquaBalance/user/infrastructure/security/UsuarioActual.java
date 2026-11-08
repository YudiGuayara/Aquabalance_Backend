package com.AquaBalance.user.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Da acceso al usuario autenticado de la petición actual.
 * El id sale del token JWT (ya validado por JwtAuthenticationFilter), nunca del cuerpo
 * de la petición, para que nadie pueda registrar datos "a nombre de" otro usuario.
 */
public final class UsuarioActual {

    private UsuarioActual() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails usuario
                && usuario.getId() != null) {
            return usuario.getId();
        }
        // No debería ocurrir: SecurityConfig exige JWT en estas rutas.
        throw new IllegalStateException("No hay un usuario autenticado en la petición");
    }
}
