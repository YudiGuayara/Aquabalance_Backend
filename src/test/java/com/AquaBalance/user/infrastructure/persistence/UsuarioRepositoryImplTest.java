package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioRepositoryImpl - Pruebas Unitarias")
class UsuarioRepositoryImplTest {

    @Mock
    private JpaUsuarioRepository jpaRepository;

    @InjectMocks
    private UsuarioRepositoryImpl usuarioRepository;

    private final LocalDateTime fecha = LocalDateTime.of(2025, 1, 15, 10, 30);

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Debe guardar el usuario y mapearlo correctamente")
        void debeGuardarYMapearCorrectamente() {
            Usuario usuario = new Usuario(null, "Carlos Pérez", "carlos@aqua.com", "hash123", Rol.Operador);
            usuario.setActivo(true);
            usuario.setFechaCreacion(fecha);

            UsuarioEntity entityGuardada = new UsuarioEntity(
                    10L,
                    "Carlos Pérez",
                    "carlos@aqua.com",
                    "hash123",
                    Rol.Operador,
                    true,
                    fecha
            );

            when(jpaRepository.save(any(UsuarioEntity.class))).thenReturn(entityGuardada);

            Usuario resultado = usuarioRepository.save(usuario);

            ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
            verify(jpaRepository).save(captor.capture());

            UsuarioEntity enviada = captor.getValue();
            assertThat(enviada.getNombre()).isEqualTo("Carlos Pérez");
            assertThat(enviada.getEmail()).isEqualTo("carlos@aqua.com");
            assertThat(enviada.getPassword()).isEqualTo("hash123");
            assertThat(enviada.getRol()).isEqualTo(Rol.Operador);
            assertThat(enviada.isActivo()).isTrue();
            assertThat(enviada.getFechaCreacion()).isEqualTo(fecha);

            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getNombre()).isEqualTo("Carlos Pérez");
            assertThat(resultado.getEmail()).isEqualTo("carlos@aqua.com");
            assertThat(resultado.getPassword()).isEqualTo("hash123");
            assertThat(resultado.getRol()).isEqualTo(Rol.Operador);
            assertThat(resultado.isActivo()).isTrue();
            assertThat(resultado.getFechaCreacion()).isEqualTo(fecha);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Debe retornar el usuario cuando existe")
        void debeRetornarUsuarioCuandoExiste() {
            UsuarioEntity entity = new UsuarioEntity(
                    1L, "Laura", "laura@aqua.com", "hash", Rol.Administrador, true, fecha
            );
            when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Usuario> resultado = usuarioRepository.findById(1L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(1L);
            assertThat(resultado.get().getNombre()).isEqualTo("Laura");
            assertThat(resultado.get().isActivo()).isTrue();
            assertThat(resultado.get().getFechaCreacion()).isEqualTo(fecha);
            verify(jpaRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe retornar vacío cuando no existe")
        void debeRetornarVacioCuandoNoExiste() {
            when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Usuario> resultado = usuarioRepository.findById(99L);

            assertThat(resultado).isEmpty();
            verify(jpaRepository).findById(99L);
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("Debe retornar el usuario cuando el email existe")
        void debeRetornarUsuarioCuandoExiste() {
            UsuarioEntity entity = new UsuarioEntity(
                    2L, "Pedro", "pedro@aqua.com", "hash2", Rol.Operador, false, fecha
            );
            when(jpaRepository.findByEmail("pedro@aqua.com")).thenReturn(Optional.of(entity));

            Optional<Usuario> resultado = usuarioRepository.findByEmail("pedro@aqua.com");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getEmail()).isEqualTo("pedro@aqua.com");
            assertThat(resultado.get().isActivo()).isFalse();
            verify(jpaRepository).findByEmail("pedro@aqua.com");
        }

        @Test
        @DisplayName("Debe retornar vacío cuando el email no existe")
        void debeRetornarVacioCuandoNoExiste() {
            when(jpaRepository.findByEmail("no@aqua.com")).thenReturn(Optional.empty());

            Optional<Usuario> resultado = usuarioRepository.findByEmail("no@aqua.com");

            assertThat(resultado).isEmpty();
            verify(jpaRepository).findByEmail("no@aqua.com");
        }
    }

    @Nested
    @DisplayName("findAllActivos()")
    class FindAllActivos {

        @Test
        @DisplayName("Debe retornar solo usuarios activos")
        void debeRetornarSoloActivos() {
            List<UsuarioEntity> entities = List.of(
                    new UsuarioEntity(1L, "Act1", "act1@aqua.com", "p1", Rol.Operador, true, fecha),
                    new UsuarioEntity(2L, "Act2", "act2@aqua.com", "p2", Rol.UsuarioPublico, true, fecha)
            );
            when(jpaRepository.findByActivoTrue()).thenReturn(entities);

            List<Usuario> resultado = usuarioRepository.findAllActivos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(Usuario::getEmail)
                    .containsExactly("act1@aqua.com", "act2@aqua.com");
            verify(jpaRepository).findByActivoTrue();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Debe retornar todos los usuarios")
        void debeRetornarTodos() {
            List<UsuarioEntity> entities = List.of(
                    new UsuarioEntity(1L, "U1", "u1@aqua.com", "p1", Rol.Administrador, true, fecha),
                    new UsuarioEntity(2L, "U2", "u2@aqua.com", "p2", Rol.Operador, false, fecha)
            );
            when(jpaRepository.findAll()).thenReturn(entities);

            List<Usuario> resultado = usuarioRepository.findAll();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getEmail()).isEqualTo("u1@aqua.com");
            assertThat(resultado.get(0).isActivo()).isTrue();
            assertThat(resultado.get(1).getEmail()).isEqualTo("u2@aqua.com");
            assertThat(resultado.get(1).isActivo()).isFalse();
            verify(jpaRepository).findAll();
        }
    }
}