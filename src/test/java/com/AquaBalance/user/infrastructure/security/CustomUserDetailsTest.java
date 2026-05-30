package com.AquaBalance.user.infrastructure.security;

import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomUserDetails - Pruebas Unitarias")
class CustomUserDetailsTest {

    private final Usuario usuario = new Usuario(1L, "Juan Pérez", "juan@aqua.com", "hash123", Rol.Operador);
    private final CustomUserDetails userDetails = new CustomUserDetails(usuario);

    @Nested
    @DisplayName("getAuthorities()")
    class Authorities {

        @Test
        @DisplayName("Debe retornar el rol con prefijo ROLE_")
        void debeRetornarRolConPrefijo() {
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority())
                    .isEqualTo("ROLE_Operador");
        }
    }

    @Nested
    @DisplayName("Datos básicos")
    class DatosBasicos {

        @Test
        @DisplayName("Debe retornar el email como username")
        void debeRetornarEmailComoUsername() {
            assertThat(userDetails.getUsername()).isEqualTo("juan@aqua.com");
        }

        @Test
        @DisplayName("Debe retornar la contraseña del usuario")
        void debeRetornarPassword() {
            assertThat(userDetails.getPassword()).isEqualTo("hash123");
        }
    }

    @Nested
    @DisplayName("Estado de la cuenta")
    class EstadoDeLaCuenta {

        @Test
        @DisplayName("La cuenta no debe expirar")
        void cuentaNoExpira() {
            assertThat(userDetails.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("La cuenta no debe estar bloqueada")
        void cuentaNoBloqueada() {
            assertThat(userDetails.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Las credenciales no deben expirar")
        void credencialesNoExpiran() {
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Debe estar habilitado cuando el usuario está activo")
        void debeEstarHabilitadoSiActivo() {
            assertThat(userDetails.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Debe estar deshabilitado cuando el usuario está inactivo")
        void debeEstarDeshabilitadoSiInactivo() {
            usuario.desactivar();

            assertThat(userDetails.isEnabled()).isFalse();
        }
    }
}