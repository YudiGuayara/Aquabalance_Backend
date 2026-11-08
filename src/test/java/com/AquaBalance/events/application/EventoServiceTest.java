package com.AquaBalance.events.application;

import com.AquaBalance.events.application.ports.out.EventoRepositoryPort;
import com.AquaBalance.events.domain.Evento;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventoService - Pruebas Unitarias")
class EventoServiceTest {

    @Mock
    private EventoRepositoryPort repositoryPort;

    @InjectMocks
    private EventoService service;

    private EventoDTO    dtoValido;
    private Evento       entidadGuardada;

    @BeforeEach
    void setUp() {
        dtoValido = new EventoDTO(null, "Desbordamiento detectado",
                "Alta", null, 1L, 1L);

        entidadGuardada = new Evento(1L, "Desbordamiento detectado",
                "Alta", LocalDateTime.of(2025, 3, 5, 14, 30), 1L, 1L);
    }

    // ================================================================
    // CREAR
    // ================================================================
    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear un evento y asignarle la fecha actual")
        void debeCrearEventoYAsignarFecha() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);

            EventoDTO resultado = service.crear(dtoValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getDescripcion()).isEqualTo("Desbordamiento detectado");
            assertThat(resultado.getMagnitud()).isEqualTo("Alta");
            assertThat(resultado.getFecha()).isNotNull();
            verify(repositoryPort).guardar(any());
        }

        @Test
        @DisplayName("Debe asignar la fecha actual al DTO antes de guardar")
        void debeAsignarFechaAntesDeguardar() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            assertThat(dtoValido.getFecha()).isNull();

            service.crear(dtoValido);

            assertThat(dtoValido.getFecha()).isNotNull();
        }
    }

    // ================================================================
    // BUSCAR POR ID
    // ================================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar el evento cuando el ID existe")
        void debeRetornarEventoCuandoExiste() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));

            EventoDTO resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getDescripcion()).isEqualTo("Desbordamiento detectado");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Evento no encontrado con id: 99");
        }
    }

    // ================================================================
    // LISTAR TODOS
    // ================================================================
    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todos los eventos registrados")
        void debeRetornarTodosLosEventos() {
            Evento e2 = new Evento(2L, "Contaminación elevada", "Media",
                    LocalDateTime.now(), 2L, 2L);
            when(repositoryPort.listarTodos()).thenReturn(List.of(entidadGuardada, e2));

            List<EventoDTO> resultado = service.listarTodos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(EventoDTO::getDescripcion)
                    .containsExactly("Desbordamiento detectado", "Contaminación elevada");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay eventos")
        void debeRetornarListaVaciasiNoHayDatos() {
            when(repositoryPort.listarTodos()).thenReturn(List.of());

            assertThat(service.listarTodos()).isEmpty();
        }
    }

    // ================================================================
    // LISTAR POR RECURSO
    // ================================================================
    @Nested
    @DisplayName("listarPorRecurso()")
    class ListarPorRecurso {

        @Test
        @DisplayName("Debe retornar eventos filtrados por recurso")
        void debeRetornarEventosPorRecurso() {
            when(repositoryPort.buscarPorRecurso(1L)).thenReturn(List.of(entidadGuardada));

            List<EventoDTO> resultado = service.listarPorRecurso(1L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getIdRecurso()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay eventos para ese recurso")
        void debeRetornarListaVaciaSiNoHayEventosPorRecurso() {
            when(repositoryPort.buscarPorRecurso(99L)).thenReturn(List.of());

            assertThat(service.listarPorRecurso(99L)).isEmpty();
        }
    }

    // ================================================================
    // ACTUALIZAR
    // ================================================================
    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar descripción y magnitud de un evento existente")
        void debeActualizarEventoExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            Evento actualizado = new Evento(1L, "Nueva descripción", "Baja",
                    LocalDateTime.now(), 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(actualizado);

            EventoDTO dto = new EventoDTO(null, "Nueva descripción", "Baja",
                    null, 1L, 1L);
            EventoDTO resultado = service.actualizar(1L, dto);

            assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción");
            assertThat(resultado.getMagnitud()).isEqualTo("Baja");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.actualizar(99L, dtoValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // ELIMINAR
    // ================================================================
    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar un evento existente sin excepción")
        void debeEliminarEventoExistente() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            doNothing().when(repositoryPort).eliminar(1L);

            assertThatNoException().isThrownBy(() -> service.eliminar(1L));
            verify(repositoryPort).eliminar(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException y NO llamar eliminar si no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repositoryPort, never()).eliminar(anyLong());
        }
    }
}
