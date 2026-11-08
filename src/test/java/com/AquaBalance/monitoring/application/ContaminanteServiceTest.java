package com.AquaBalance.monitoring.application;

import com.AquaBalance.monitoring.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.NivelContaminante;
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
@DisplayName("ContaminanteService - Pruebas Unitarias")
class ContaminanteServiceTest {

    @Mock
    private ContaminanteRepositoryPort repositoryPort;

    @InjectMocks
    private ContaminanteService service;

    // ---------------------------------------------------------------
    // Datos de prueba reutilizables
    // ---------------------------------------------------------------
    private ContaminanteDTO dtoValido;
    private Contaminante entidadGuardada;

    @BeforeEach
    void setUp() {
        dtoValido = new ContaminanteDTO(null, "Mercurio", 5.0, NivelContaminante.Alto, "Industria minera");
        entidadGuardada = new Contaminante(1L, "Mercurio", 5.0, NivelContaminante.Alto, "Industria minera");
    }

    // ===============================================================
    // CREAR
    // ===============================================================
    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear un contaminante válido y retornar el DTO con ID asignado")
        void debeCrearContaminanteValido() {
            when(repositoryPort.guardar(any(Contaminante.class))).thenReturn(entidadGuardada);

            ContaminanteDTO resultado = service.crear(dtoValido);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Mercurio");
            assertThat(resultado.getCarga()).isEqualTo(5.0);
            assertThat(resultado.getNivel()).isEqualTo(NivelContaminante.Alto);
            assertThat(resultado.getFuenteOrigen()).isEqualTo("Industria minera");
            verify(repositoryPort).guardar(any(Contaminante.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el DTO es nulo")
        void debeLanzarExcepcionSiDtoEsNulo() {
            assertThatThrownBy(() -> service.crear(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El contaminante no puede ser nulo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre está vacío")
        void debeLanzarExcepcionSiNombreEstaVacio() {
            dtoValido.setNombre("");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre es demasiado corto (menos de 3 caracteres)")
        void debeLanzarExcepcionSiNombreEsCorto() {
            dtoValido.setNombre("AB");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre debe tener al menos 3 caracteres");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la carga es nula")
        void debeLanzarExcepcionSiCargaEsNula() {
            dtoValido.setCarga(null);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La carga es obligatoria");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la carga es negativa")
        void debeLanzarExcepcionSiCargaEsNegativa() {
            dtoValido.setCarga(-1.0);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La carga no puede ser negativa");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nivel es nulo")
        void debeLanzarExcepcionSiNivelEsNulo() {
            dtoValido.setNivel(null);
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nivel es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la fuente de origen está vacía")
        void debeLanzarExcepcionSiFuenteOrigenEstaVacia() {
            dtoValido.setFuenteOrigen("   ");
            assertThatThrownBy(() -> service.crear(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La fuente de origen es obligatoria");
        }

        @Test
        @DisplayName("Debe aceptar carga igual a cero (límite válido)")
        void debeAceptarCargaCero() {
            dtoValido.setCarga(0.0);
            Contaminante entidadCargaCero = new Contaminante(2L, "Mercurio", 0.0, NivelContaminante.Bajo, "Industria minera");
            when(repositoryPort.guardar(any())).thenReturn(entidadCargaCero);

            ContaminanteDTO resultado = service.crear(dtoValido);

            assertThat(resultado.getCarga()).isEqualTo(0.0);
        }
    }

    // ===============================================================
    // ACTUALIZAR
    // ===============================================================
    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar un contaminante existente")
        void debeActualizarContaminanteExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            Contaminante actualizado = new Contaminante(1L, "Mercurio actualizado", 7.0, NivelContaminante.Critico, "Nueva fuente");
            when(repositoryPort.guardar(any())).thenReturn(actualizado);

            ContaminanteDTO dto = new ContaminanteDTO(null, "Mercurio actualizado", 7.0, NivelContaminante.Critico, "Nueva fuente");
            ContaminanteDTO resultado = service.actualizar(1L, dto);

            assertThat(resultado.getNombre()).isEqualTo("Mercurio actualizado");
            assertThat(resultado.getCarga()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el contaminante no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.actualizar(99L, dtoValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contaminante no encontrado con id: 99");
        }
    }

    // ===============================================================
    // BUSCAR POR ID
    // ===============================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar el contaminante cuando existe")
        void debeRetornarContaminanteCuandoExiste() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));

            ContaminanteDTO resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Mercurio");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ===============================================================
    // LISTAR TODOS
    // ===============================================================
    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar lista de contaminantes")
        void debeRetornarListaDeContaminantes() {
            Contaminante c2 = new Contaminante(2L, "Plomo", 3.5, NivelContaminante.Medio, "Tuberías");
            when(repositoryPort.listarTodos()).thenReturn(List.of(entidadGuardada, c2));

            List<ContaminanteDTO> resultado = service.listarTodos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(ContaminanteDTO::getNombre)
                    .containsExactly("Mercurio", "Plomo");
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay contaminantes")
        void debeRetornarListaVaciaSiNoHayDatos() {
            when(repositoryPort.listarTodos()).thenReturn(List.of());

            List<ContaminanteDTO> resultado = service.listarTodos();

            assertThat(resultado).isEmpty();
        }
    }

    // ===============================================================
    // ELIMINAR
    // ===============================================================
    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar un contaminante existente sin lanzar excepción")
        void debeEliminarContaminanteExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            doNothing().when(repositoryPort).eliminar(1L);

            assertThatNoException().isThrownBy(() -> service.eliminar(1L));
            verify(repositoryPort).eliminar(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al eliminar un ID inexistente")
        void debeLanzarExcepcionAlEliminarIdInexistente() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repositoryPort, never()).eliminar(anyLong());
        }
    }
}
