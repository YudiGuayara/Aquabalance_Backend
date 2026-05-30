package com.AquaBalance.user.infrastructure.security;

import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Pruebas Unitarias")
class CustomUserDetailsServiceTest {

    @Mock
    private BuscarUsuarioUseCase buscarUsuarioUseCase;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debe cargar el usuario por email y devolver un UserDetails válido")
    void debeCargarUsuarioPorEmail() {
        Usuario usuario = new Usuario(1L, "Ana López", "ana@aqua.com", "hash456", Rol.Administrador);

        when(buscarUsuarioUseCase.buscarPorEmail("ana@aqua.com")).thenReturn(usuario);

        UserDetails resultado = customUserDetailsService.loadUserByUsername("ana@aqua.com");

        assertThat(resultado).isInstanceOf(CustomUserDetails.class);
        assertThat(resultado.getUsername()).isEqualTo("ana@aqua.com");
        assertThat(resultado.getPassword()).isEqualTo("hash456");
        assertThat(resultado.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_Administrador");

        verify(buscarUsuarioUseCase).buscarPorEmail("ana@aqua.com");
    }

    @Test
    @DisplayName("Debe lanzar UsernameNotFoundException cuando el usuario no existe")
    void debeLanzarExcepcionCuandoNoExiste() {
        when(buscarUsuarioUseCase.buscarPorEmail("noexiste@aqua.com"))
                .thenThrow(new RuntimeException("No encontrado"));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("noexiste@aqua.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("noexiste@aqua.com");

        verify(buscarUsuarioUseCase).buscarPorEmail("noexiste@aqua.com");
    }
}