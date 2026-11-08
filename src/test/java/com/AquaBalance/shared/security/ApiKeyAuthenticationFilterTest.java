package com.AquaBalance.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Seguridad de sensores (X-API-Key)")
class ApiKeyAuthenticationFilterTest {

    private static final String CLAVE = "clave-de-prueba-123";

    @Mock private HttpServletRequest  request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain         filterChain;
    @Mock private JwtService          jwtService;
    @Mock private UserDetailsService  userDetailsService;

    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new ApiKeyAuthenticationFilter(CLAVE);
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/api/estaciones/3/telemetria");
    }

    @Test
    @DisplayName("Con la clave correcta en una ruta de sensor autentica como ROLE_SENSOR")
    void claveValidaAutentica() throws Exception {
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        filter.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_SENSOR");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Funciona tambien en /perdida")
    void claveValidaEnPerdida() throws Exception {
        when(request.getServletPath()).thenReturn("/api/estaciones/12/perdida");
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Clave incorrecta -> 401 y no continua la cadena")
    void claveIncorrectaRechaza() throws Exception {
        when(request.getHeader("X-API-Key")).thenReturn("otra-clave");

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Si el servidor no tiene clave configurada, ninguna clave sirve")
    void sinClaveConfiguradaRechazaTodo() throws Exception {
        filter = new ApiKeyAuthenticationFilter("");
        when(request.getHeader("X-API-Key")).thenReturn("");

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("La clave NO concede acceso a rutas que no son de sensores")
    void claveNoValeEnOtrasRutas() throws Exception {
        when(request.getServletPath()).thenReturn("/api/usuarios");
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("La clave NO vale para GET en rutas de sensor")
    void claveNoValeParaGet() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("No se acepta un id que no sea numerico ni rutas con sufijos")
    void rutaEstricta() throws Exception {
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        when(request.getServletPath()).thenReturn("/api/estaciones/abc/telemetria");
        filter.doFilter(request, response, filterChain);
        when(request.getServletPath()).thenReturn("/api/estaciones/3/telemetria/../../usuarios");
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Sin cabecera no interviene")
    void sinCabecera() throws Exception {
        when(request.getHeader("X-API-Key")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtAuthenticationFilter deja pasar la llamada de sensor con X-API-Key (sin Bearer)")
    void jwtFilterDejaPasarSensores() throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);

        jwtFilter.doFilter(request, response, filterChain);

        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtAuthenticationFilter sigue exigiendo Bearer fuera de las rutas de sensor, aunque venga X-API-Key")
    void jwtFilterExigeBearerEnOtrasRutas() throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        when(request.getServletPath()).thenReturn("/api/usuarios");
        when(request.getHeader("X-API-Key")).thenReturn(CLAVE);
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }
}
