package com.AquaBalance.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Pruebas Unitarias")
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest  request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter filter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userDetails = User.withUsername("laura@aqua.com")
                .password("hashed").authorities(Collections.emptyList()).build();
    }

    // ================================================================
    // Rutas públicas
    // ================================================================
    @Nested
    @DisplayName("Rutas públicas /api/auth/**")
    class RutasPublicas {

        @Test
        @DisplayName("Debe continuar la cadena sin validar token para /api/auth/login")
        void debePermitirAuthLogin() throws Exception {
            when(request.getServletPath()).thenReturn("/api/auth/login");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(jwtService, never()).extractUsername(any());
        }

        @Test
        @DisplayName("Debe continuar la cadena sin validar token para /api/auth/registro")
        void debePermitirAuthRegistro() throws Exception {
            when(request.getServletPath()).thenReturn("/api/auth/registro");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    // ================================================================
    // Token ausente o malformado
    // ================================================================
    @Nested
    @DisplayName("Token ausente o sin formato Bearer")
    class TokenAusente {

        @Test
        @DisplayName("Debe retornar 401 si no hay cabecera Authorization")
        void debeRetornar401SiNoHayHeader() throws Exception {
            when(request.getServletPath()).thenReturn("/api/alertas");
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Debe retornar 401 si el header no empieza con 'Bearer '")
        void debeRetornar401SiNoEsBearer() throws Exception {
            when(request.getServletPath()).thenReturn("/api/alertas");
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }
    }

    // ================================================================
    // Token válido
    // ================================================================
    @Nested
    @DisplayName("Token JWT válido")
    class TokenValido {

        @Test
        @DisplayName("Debe autenticar al usuario y continuar la cadena con token válido")
        void debeAutenticarConTokenValido() throws Exception {
            when(request.getServletPath()).thenReturn("/api/alertas");
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
            when(jwtService.extractUsername("valid.jwt.token")).thenReturn("laura@aqua.com");
            when(userDetailsService.loadUserByUsername("laura@aqua.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("laura@aqua.com");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Debe continuar la cadena pero sin autenticar si el token no pasa validación")
        void debeContinuarSinAutenticarSiTokenInvalido() throws Exception {
            when(request.getServletPath()).thenReturn("/api/alertas");
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");
            when(jwtService.extractUsername("invalid.jwt.token")).thenReturn("laura@aqua.com");
            when(userDetailsService.loadUserByUsername("laura@aqua.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid("invalid.jwt.token", userDetails)).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            // No se establece autenticación pero la cadena continúa
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Debe continuar la cadena sin autenticar si extractUsername retorna null")
        void debeContinuarSiUsernameEsNull() throws Exception {
            when(request.getServletPath()).thenReturn("/api/recursos");
            when(request.getHeader("Authorization")).thenReturn("Bearer token.con.null");
            when(jwtService.extractUsername("token.con.null")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }
    }

    // ================================================================
    // Excepciones en el proceso
    // ================================================================
    @Nested
    @DisplayName("Excepciones durante el procesamiento del token")
    class ExcepcionesToken {

        @Test
        @DisplayName("Debe retornar 401 si extractUsername lanza una excepción (token corrupto)")
        void debeRetornar401SiTokenCorrupto() throws Exception {
            when(request.getServletPath()).thenReturn("/api/mediciones");
            when(request.getHeader("Authorization")).thenReturn("Bearer corrupto.jwt.token");
            when(jwtService.extractUsername("corrupto.jwt.token"))
                    .thenThrow(new RuntimeException("Token malformado"));

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }
    }
}
