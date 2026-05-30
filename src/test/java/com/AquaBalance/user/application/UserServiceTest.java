// UserServiceTest.java
package com.AquaBalance.user.application;

import com.AquaBalance.shared.exception.BusinessException;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas Unitarias")
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // ← faltaba esto

    @InjectMocks
    private UserService service;

    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = new Usuario(1L, "Carlos Pérez", "carlos@aqua.com", "hash123", Rol.Operador);
        usuarioInactivo = new Usuario(2L, "Ana López", "ana@aqua.com", "hash456", Rol.UsuarioPublico);
        usuarioInactivo.desactivar();
    }

    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("Debe registrar un usuario cuando el correo no existe aún")
        void debeRegistrarUsuarioNuevo() {
            when(usuarioRepository.findByEmail("carlos@aqua.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash_encriptado"); // ← mock del encode
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

            Usuario resultado = service.registrar(usuarioActivo);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Carlos Pérez");
            assertThat(resultado.getEmail()).isEqualTo("carlos@aqua.com");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando el correo ya está registrado")
        void debeLanzarExcepcionSiCorreoYaExiste() {
            when(usuarioRepository.findByEmail("carlos@aqua.com")).thenReturn(Optional.of(usuarioActivo));

            assertThatThrownBy(() -> service.registrar(usuarioActivo))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya está registrado");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar el usuario cuando el ID existe")
        void debeRetornarUsuarioPorId() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo));

            Usuario resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Carlos Pérez");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
        void debeLanzarExcepcionSiIdNoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("buscarPorEmail()")
    class BuscarPorEmail {

        @Test
        @DisplayName("Debe retornar el usuario cuando el correo existe")
        void debeRetornarUsuarioPorEmail() {
            when(usuarioRepository.findByEmail("carlos@aqua.com")).thenReturn(Optional.of(usuarioActivo));

            Usuario resultado = service.buscarPorEmail("carlos@aqua.com");

            assertThat(resultado.getEmail()).isEqualTo("carlos@aqua.com");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el correo no existe")
        void debeLanzarExcepcionSiEmailNoExiste() {
            when(usuarioRepository.findByEmail("noexiste@aqua.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorEmail("noexiste@aqua.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("noexiste@aqua.com");
        }
    }

    @Nested
    @DisplayName("listarActivos()")
    class ListarActivos {

        @Test
        @DisplayName("Debe retornar solo los usuarios activos")
        void debeRetornarSoloUsuariosActivos() {
            when(usuarioRepository.findAllActivos()).thenReturn(List.of(usuarioActivo));

            List<Usuario> resultado = service.listarActivos();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).isActivo()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay usuarios activos")
        void debeRetornarListaVaciaSiNoHayActivos() {
            when(usuarioRepository.findAllActivos()).thenReturn(List.of());

            assertThat(service.listarActivos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("desactivarUsuario()")
    class DesactivarUsuario {

        @Test
        @DisplayName("Debe desactivar un usuario activo correctamente")
        void debeDesactivarUsuarioActivo() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

            service.desactivarUsuario(1L);

            assertThat(usuarioActivo.isActivo()).isFalse();
            verify(usuarioRepository).save(usuarioActivo);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
        void debeLanzarExcepcionSiUsuarioNoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.desactivarUsuario(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("activarUsuario()")
    class ActivarUsuario {

        @Test
        @DisplayName("Debe activar un usuario inactivo correctamente")
        void debeActivarUsuarioInactivo() {
            when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioInactivo));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioInactivo);

            service.activarUsuario(2L);

            assertThat(usuarioInactivo.isActivo()).isTrue();
            verify(usuarioRepository).save(usuarioInactivo);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
        void debeLanzarExcepcionSiUsuarioNoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.activarUsuario(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Lógica de dominio - Usuario.activar() y desactivar()")
    class LogicaDominio {

        @Test
        @DisplayName("Un usuario nuevo debe nacer activo por defecto")
        void usuarioNuevoNaceActivo() {
            Usuario nuevo = new Usuario(3L, "Laura", "laura@aqua.com", "pwd", Rol.Administrador);
            assertThat(nuevo.isActivo()).isTrue();
        }

        @Test
        @DisplayName("desactivar() debe cambiar activo a false")
        void desactivarCambiaActivoAFalse() {
            usuarioActivo.desactivar();
            assertThat(usuarioActivo.isActivo()).isFalse();
        }

        @Test
        @DisplayName("activar() debe cambiar activo a true")
        void activarCambiaActivoATrue() {
            usuarioInactivo.activar();
            assertThat(usuarioInactivo.isActivo()).isTrue();
        }
    }
}