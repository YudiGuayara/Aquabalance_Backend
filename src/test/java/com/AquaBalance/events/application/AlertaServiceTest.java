package com.AquaBalance.events.application;

import com.AquaBalance.events.application.ports.out.AlertaRepositoryPort;
import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.NivelAlerta;
import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService - Pruebas Unitarias")
class AlertaServiceTest {

    @Mock private AlertaRepositoryPort         repositoryPort;
    @Mock private GestionarNotificacionUseCase notificacionUseCase;

    @InjectMocks
    private AlertaService service;

    private AlertaDTO dtoValido;
    private Alerta    entidadGuardada;

    @BeforeEach
    void setUp() {
        dtoValido = new AlertaDTO(null, null, NivelAlerta.Roja,
                "Nivel crítico de mercurio", 1L, 1L);
        entidadGuardada = new Alerta(1L,
                LocalDateTime.of(2025, 4, 20, 8, 0),
                NivelAlerta.Roja, "Nivel crítico de mercurio", 1L, 1L);
    }

    // ================================================================
    // CREAR — mapeo de niveles
    // ================================================================
    @Nested
    @DisplayName("crear() — mapeo NivelAlerta → nivel notificación")
    class Crear {

        @Test
        @DisplayName("NivelAlerta.Roja  → notificar con 'ALTA'")
        void rojaMapeaAAlta() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));

            service.crear(dtoValido);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));
        }

        @Test
        @DisplayName("NivelAlerta.Naranja → notificar con 'ALTA'")
        void naranjaMapeaAAlta() {
            dtoValido.setNivel(NivelAlerta.Naranja);
            Alerta a = new Alerta(1L, LocalDateTime.now(), NivelAlerta.Naranja, "msg", 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(a);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));

            service.crear(dtoValido);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));
        }

        @Test
        @DisplayName("NivelAlerta.Amarilla → notificar con 'MEDIA'")
        void amarillaMapeaAMedia() {
            dtoValido.setNivel(NivelAlerta.Amarilla);
            Alerta a = new Alerta(1L, LocalDateTime.now(), NivelAlerta.Amarilla, "msg", 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(a);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));

            service.crear(dtoValido);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));
        }

        @Test
        @DisplayName("NivelAlerta.Verde → notificar con 'BAJA'")
        void verdeMapeaABaja() {
            dtoValido.setNivel(NivelAlerta.Verde);
            Alerta a = new Alerta(1L, LocalDateTime.now(), NivelAlerta.Verde, "msg", 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(a);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("BAJA"));

            service.crear(dtoValido);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("BAJA"));
        }

        @Test
        @DisplayName("Nivel nulo → notificar con 'BAJA'")
        void nivelNuloMapeaABaja() {
            dtoValido.setNivel(null);
            Alerta a = new Alerta(1L, LocalDateTime.now(), null, "msg", 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(a);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("BAJA"));

            service.crear(dtoValido);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("BAJA"));
        }

        @Test
        @DisplayName("Debe asignar fecha antes de guardar y retornar DTO con ID")
        void debeAsignarFechaYRetornarDTO() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), any());

            assertThat(dtoValido.getFecha()).isNull();
            AlertaDTO resultado = service.crear(dtoValido);

            assertThat(dtoValido.getFecha()).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
        }
    }

    // ================================================================
    // BUSCAR POR ID
    // ================================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar la alerta cuando existe")
        void debeRetornar() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            assertThat(service.buscarPorId(1L).getNivel()).isEqualTo(NivelAlerta.Roja);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no existe")
        void debeLanzarExcepcion() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // LISTAR TODOS
    // ================================================================
    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todas las alertas")
        void debeRetornarTodas() {
            Alerta a2 = new Alerta(2L, LocalDateTime.now(), NivelAlerta.Amarilla, "temp", 2L, 2L);
            when(repositoryPort.listarTodos()).thenReturn(List.of(entidadGuardada, a2));

            assertThat(service.listarTodos()).hasSize(2);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay alertas")
        void debeRetornarListaVacia() {
            when(repositoryPort.listarTodos()).thenReturn(List.of());
            assertThat(service.listarTodos()).isEmpty();
        }
    }

    // ================================================================
    // LISTAR POR NIVEL
    // ================================================================
    @Nested
    @DisplayName("listarPorNivel()")
    class ListarPorNivel {

        @Test
        @DisplayName("Debe filtrar por nivel Roja")
        void debeFiltrar() {
            when(repositoryPort.buscarPorNivel(NivelAlerta.Roja)).thenReturn(List.of(entidadGuardada));
            assertThat(service.listarPorNivel(NivelAlerta.Roja)).hasSize(1);
        }
    }

    // ================================================================
    // LISTAR POR EVENTO
    // ================================================================
    @Nested
    @DisplayName("listarPorEvento()")
    class ListarPorEvento {

        @Test
        @DisplayName("Debe retornar alertas del evento")
        void debeRetornarPorEvento() {
            when(repositoryPort.buscarPorEvento(1L)).thenReturn(List.of(entidadGuardada));
            assertThat(service.listarPorEvento(1L)).hasSize(1);
        }
    }

    // ================================================================
    // ACTUALIZAR
    // ================================================================
    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar alerta existente")
        void debeActualizar() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            Alerta actualizada = new Alerta(1L, LocalDateTime.now(), NivelAlerta.Verde, "normalizado", 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(actualizada);

            AlertaDTO dto = new AlertaDTO(null, null, NivelAlerta.Verde, "normalizado", 1L, 1L);
            AlertaDTO resultado = service.actualizar(1L, dto);

            assertThat(resultado.getNivel()).isEqualTo(NivelAlerta.Verde);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no existe")
        void debeLanzarExcepcion() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.actualizar(99L, dtoValido))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // ELIMINAR
    // ================================================================
    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar si existe")
        void debeEliminar() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            doNothing().when(repositoryPort).eliminar(1L);

            assertThatNoException().isThrownBy(() -> service.eliminar(1L));
            verify(repositoryPort).eliminar(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no existe")
        void debeLanzarExcepcion() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(repositoryPort, never()).eliminar(anyLong());
        }
    }
}
