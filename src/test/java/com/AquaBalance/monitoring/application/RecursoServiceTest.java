package com.AquaBalance.monitoring.application;

import com.AquaBalance.monitoring.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoring.domain.Recurso;
import com.AquaBalance.monitoring.domain.TipoRecurso;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecursoService - Pruebas Unitarias")
class RecursoServiceTest {

    @Mock
    private RecursoRepositoryPort repositoryPort;

    @InjectMocks
    private RecursoService service;

    // ---------------------------------------------------------------
    // Datos de prueba reutilizables
    // ---------------------------------------------------------------
    private RecursoDTO dtoValido;
    private Recurso entidadGuardada;

    @BeforeEach
    void setUp() {
        // Coordenadas válidas: Bogotá, Colombia
        dtoValido = new RecursoDTO(null, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", 4.6097, -74.0817);
        entidadGuardada = new Recurso(1L, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", 4.6097, -74.0817);
    }

    // ===============================================================
    // CREAR
    // ===============================================================
    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear un recurso válido y retornar el DTO con ID asignado")
        void debeCrearRecursoValido() {
            when(repositoryPort.guardar(any(Recurso.class))).thenReturn(entidadGuardada);

            RecursoDTO resultado = service.crear(dtoValido);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Río Bogotá");
            assertThat(resultado.getTipo()).isEqualTo(TipoRecurso.Rio);
            assertThat(resultado.getUbicacion()).isEqualTo("Cundinamarca");
            assertThat(resultado.getLatitud()).isEqualTo(4.6097);
            assertThat(resultado.getLongitud()).isEqualTo(-74.0817);
            verify(repositoryPort).guardar(any(Recurso.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el DTO es nulo")
        void debeLanzarExcepcionSiDtoEsNulo() {
            assertThatThrownBy(() -> service.crear(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El recurso no puede ser nulo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre está vacío")
        void debeLanzarExcepcionSiNombreEstaVacio() {
            dtoValido.setNombre("");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre del recurso es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre tiene menos de 3 caracteres")
        void debeLanzarExcepcionSiNombreEsCorto() {
            dtoValido.setNombre("Ri");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre debe tener al menos 3 caracteres");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tipo es nulo")
        void debeLanzarExcepcionSiTipoEsNulo() {
            dtoValido.setTipo(null);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El tipo de recurso es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la ubicación está vacía")
        void debeLanzarExcepcionSiUbicacionEstaVacia() {
            dtoValido.setUbicacion("   ");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La ubicación es obligatoria");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la latitud es nula")
        void debeLanzarExcepcionSiLatitudEsNula() {
            dtoValido.setLatitud(null);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La latitud es obligatoria");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la longitud es nula")
        void debeLanzarExcepcionSiLongitudEsNula() {
            dtoValido.setLongitud(null);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La longitud es obligatoria");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la latitud está fuera del rango [-90, 90]")
        void debeLanzarExcepcionSiLatitudEsInvalida() {
            dtoValido.setLatitud(91.0);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Latitud inválida");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la longitud está fuera del rango [-180, 180]")
        void debeLanzarExcepcionSiLongitudEsInvalida() {
            dtoValido.setLongitud(181.0);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Longitud inválida");
        }

        @Test
        @DisplayName("Debe aceptar latitud en el límite inferior (-90)")
        void debeAceptarLatitudEnLimiteInferior() {
            dtoValido.setLatitud(-90.0);
            Recurso entidad = new Recurso(2L, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", -90.0, -74.0817);
            when(repositoryPort.guardar(any())).thenReturn(entidad);

            RecursoDTO resultado = service.crear(dtoValido);
            assertThat(resultado.getLatitud()).isEqualTo(-90.0);
        }

        @Test
        @DisplayName("Debe aceptar todos los tipos de recurso disponibles")
        void debeAceptarTodosLosTiposDeRecurso() {
            for (TipoRecurso tipo : TipoRecurso.values()) {
                dtoValido.setTipo(tipo);
                Recurso entidad = new Recurso(1L, "Río Bogotá", tipo, "Cundinamarca", 4.6097, -74.0817);
                when(repositoryPort.guardar(any())).thenReturn(entidad);

                RecursoDTO resultado = service.crear(dtoValido);
                assertThat(resultado.getTipo()).isEqualTo(tipo);
            }
        }
    }

    // ===============================================================
    // ACTUALIZAR
    // ===============================================================
    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar un recurso existente correctamente")
        void debeActualizarRecursoExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            RecursoDTO dtoActualizado = new RecursoDTO(null, "Lago Calima", TipoRecurso.Lago, "Valle del Cauca", 3.9283, -76.4911);
            Recurso actualizado = new Recurso(1L, "Lago Calima", TipoRecurso.Lago, "Valle del Cauca", 3.9283, -76.4911);
            when(repositoryPort.guardar(any())).thenReturn(actualizado);

            RecursoDTO resultado = service.actualizar(1L, dtoActualizado);

            assertThat(resultado.getNombre()).isEqualTo("Lago Calima");
            assertThat(resultado.getTipo()).isEqualTo(TipoRecurso.Lago);
            assertThat(resultado.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void debeLanzarExcepcionSiIdNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.actualizar(99L, dtoValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Recurso no encontrado con id: 99");
        }
    }

    // ===============================================================
    // BUSCAR POR ID
    // ===============================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar el recurso cuando existe")
        void debeRetornarRecursoCuandoExiste() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));

            RecursoDTO resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Río Bogotá");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando no existe el ID")
        void debeLanzarExcepcionCuandoNoExiste() {
            when(repositoryPort.buscarPorId(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(42L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("42");
        }
    }

    // ===============================================================
    // LISTAR TODOS
    // ===============================================================
    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todos los recursos disponibles")
        void debeRetornarTodosLosRecursos() {
            Recurso r2 = new Recurso(2L, "Embalse del Muña", TipoRecurso.Embalse, "Cundinamarca", 4.5, -74.2);
            when(repositoryPort.listarTodos()).thenReturn(List.of(entidadGuardada, r2));

            List<RecursoDTO> resultado = service.listarTodos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(RecursoDTO::getNombre)
                    .containsExactly("Río Bogotá", "Embalse del Muña");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay recursos registrados")
        void debeRetornarListaVaciaSiNoHayDatos() {
            when(repositoryPort.listarTodos()).thenReturn(List.of());

            assertThat(service.listarTodos()).isEmpty();
        }
    }

    // ===============================================================
    // ELIMINAR
    // ===============================================================
    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar un recurso existente sin lanzar excepción")
        void debeEliminarRecursoExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            doNothing().when(repositoryPort).eliminar(1L);

            assertThatNoException().isThrownBy(() -> service.eliminar(1L));
            verify(repositoryPort).eliminar(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar un ID inexistente")
        void debeLanzarExcepcionAlEliminarIdInexistente() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repositoryPort, never()).eliminar(anyLong());
        }
    }
}
