package com.AquaBalance.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias puras para JwtService.
 * No necesita contexto de Spring — solo instanciamos la clase directamente.
 */
@DisplayName("JwtService - Pruebas Unitarias")
class JwtServiceTest {

    private JwtService jwtService;

    // Usuario ficticio para todas las pruebas
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService  = new JwtService();
        userDetails = User.withUsername("operador@aqua.com")
                .password("hashed_password")
                .authorities(Collections.emptyList())
                .build();
    }

    // ================================================================
    // GENERAR TOKEN
    // ================================================================
    @Nested
    @DisplayName("generateToken()")
    class GenerarToken {

        @Test
        @DisplayName("Debe generar un token no nulo y no vacío")
        void debeGenerarTokenNoVacio() {
            String token = jwtService.generateToken(userDetails);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("El token generado debe tener el formato JWT (3 partes separadas por '.')")
        void debeGenerarTokenEnFormatoJwt() {
            String token = jwtService.generateToken(userDetails);
            String[] partes = token.split("\\.");

            // Un JWT válido = header.payload.signature
            assertThat(partes).hasSize(3);
        }

        @Test
        @DisplayName("Cada llamada debe generar un token diferente (distintos timestamps)")
        void debeGenerarTokensDiferentes() throws InterruptedException {
            String token1 = jwtService.generateToken(userDetails);
            Thread.sleep(1100); // esperar para que cambie el timestamp
            String token2 = jwtService.generateToken(userDetails);

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // ================================================================
    // EXTRAER USERNAME
    // ================================================================
    @Nested
    @DisplayName("extractUsername()")
    class ExtraerUsername {

        @Test
        @DisplayName("Debe extraer el username correcto del token generado")
        void debeExtraerUsernameDelToken() {
            String token = jwtService.generateToken(userDetails);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("operador@aqua.com");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el token está manipulado")
        void debeLanzarExcepcionConTokenManipulado() {
            String tokenManipulado = "header.payload_falso.firma_falsa";

            assertThatThrownBy(() -> jwtService.extractUsername(tokenManipulado))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Debe extraer username correcto para diferentes usuarios")
        void debeExtraerUsernameDeDiferentesUsuarios() {
            UserDetails admin = User.withUsername("admin@aqua.com")
                    .password("pwd").authorities(Collections.emptyList()).build();

            String tokenOperador = jwtService.generateToken(userDetails);
            String tokenAdmin    = jwtService.generateToken(admin);

            assertThat(jwtService.extractUsername(tokenOperador))
                    .isEqualTo("operador@aqua.com");
            assertThat(jwtService.extractUsername(tokenAdmin))
                    .isEqualTo("admin@aqua.com");
        }
    }

    // ================================================================
    // VALIDAR TOKEN
    // ================================================================
    @Nested
    @DisplayName("isTokenValid()")
    class ValidarToken {

        @Test
        @DisplayName("Debe retornar true cuando el token pertenece al usuario y no está expirado")
        void debeRetornarTrueConTokenValido() {
            String token = jwtService.generateToken(userDetails);

            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si el token pertenece a un usuario diferente")
        void debeRetornarFalseSiTokenEsDeOtroUsuario() {
            UserDetails otroUsuario = User.withUsername("otro@aqua.com")
                    .password("pwd").authorities(Collections.emptyList()).build();

            String tokenOtro = jwtService.generateToken(otroUsuario);

            // El token de "otro" no debe ser válido para "operador"
            assertThat(jwtService.isTokenValid(tokenOtro, userDetails)).isFalse();
        }

        @Test
        @DisplayName("Debe lanzar excepción al validar un token completamente inválido")
        void debeLanzarExcepcionConTokenInvalido() {
            assertThatThrownBy(() -> jwtService.isTokenValid("token.invalido.xyz", userDetails))
                    .isInstanceOf(Exception.class);
        }
    }

    // ================================================================
    // EXTRACT CLAIM GENÉRICO
    // ================================================================
    @Nested
    @DisplayName("extractClaim()")
    class ExtraerClaim {

        @Test
        @DisplayName("Debe extraer el subject (email) usando el resolver genérico")
        void debeExtraerSubjectConResolverGenerico() {
            String token = jwtService.generateToken(userDetails);

            String subject = jwtService.extractClaim(token,
                    claims -> claims.getSubject());

            assertThat(subject).isEqualTo("operador@aqua.com");
        }

        @Test
        @DisplayName("Debe extraer la fecha de expiración correctamente")
        void debeExtraerFechaDeExpiracion() {
            String token = jwtService.generateToken(userDetails);

            java.util.Date expiracion = jwtService.extractClaim(token,
                    claims -> claims.getExpiration());

            // El token debe expirar aproximadamente en 24h
            long diferenciaMs = expiracion.getTime() - System.currentTimeMillis();
            assertThat(diferenciaMs)
                    .isGreaterThan(0)
                    .isLessThanOrEqualTo(1000L * 60 * 60 * 24 + 1000); // máximo 24h + 1s de margen
        }
    }
}
