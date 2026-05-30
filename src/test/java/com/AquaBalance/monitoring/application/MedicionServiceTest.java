package com.AquaBalance.monitoring.application;

import com.AquaBalance.monitoring.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoring.application.ports.out.MedicionRepositoryPort;
import com.AquaBalance.monitoring.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.Medicion;
import com.AquaBalance.monitoring.domain.NivelContaminante;
import com.AquaBalance.monitoring.domain.Recurso;
import com.AquaBalance.monitoring.domain.TipoRecurso;
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
@DisplayName("MedicionService - Pruebas Unitarias")
class MedicionServiceTest {

    @Mock private MedicionRepositoryPort        repositoryPort;
    @Mock private RecursoRepositoryPort         recursoRepository;
    @Mock private ContaminanteRepositoryPort    contaminanteRepository;
    @Mock private GestionarNotificacionUseCase  notificacionUseCase;

    @InjectMocks
    private MedicionService service;

    private MedicionDTO  dtoValido;
    private Medicion     entidadGuardada;
    private Recurso      recurso;
    private Contaminante contaminante;

    @BeforeEach
    void setUp() {
        // pH=7.0 y temperatura=22.5 → dentro de rangos normales (sin alertas)
        dtoValido       = new MedicionDTO(null, 7.0, 22.5, null, 1L, 1L, 1L);
        entidadGuardada = new Medicion(1L, 7.0, 22.5, LocalDateTime.now(), 1L, 1L, 1L);
        recurso         = new Recurso(1L, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", 4.6097, -74.0817);
        contaminante    = new Contaminante(1L, "Mercurio", 5.0, NivelContaminante.Alto, "Minería");
    }

    private void mockEnrich() {
        when(recursoRepository.buscarPorId(1L)).thenReturn(Optional.of(recurso));
        when(contaminanteRepository.buscarPorId(1L)).thenReturn(Optional.of(contaminante));
    }

    // ================================================================
    // REGISTRAR
    // ================================================================
    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("Debe registrar medición válida y enriquecer nombres; sin alertas en rango normal")
        void debeRegistrarMedicionValida() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());

            MedicionDTO resultado = service.registrar(dtoValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getPh()).isEqualTo(7.0);
            assertThat(resultado.getNombreRecurso()).isEqualTo("Río Bogotá");
            assertThat(resultado.getNombreContaminante()).isEqualTo("Mercurio");
            verify(repositoryPort).guardar(any());
            verify(notificacionUseCase).notificarMedicion("Río Bogotá", "Mercurio", 7.0);
            // pH=7.0 y temp=22.5 están dentro de rangos → no disparan alerta
            verify(notificacionUseCase, never()).notificarAlerta(any(), any());
        }

        @Test
        @DisplayName("Debe asignar la fecha actual antes de guardar")
        void debeAsignarFechaActual() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());

            assertThat(dtoValido.getFecha()).isNull();
            service.registrar(dtoValido);
            assertThat(dtoValido.getFecha()).isNotNull();
        }

        @Test
        @DisplayName("Debe notificar alerta ALTA cuando el pH es críticamente bajo (< 5.0)")
        void debeNotificarAlertaAltaSiPhCriticamenteBajo() {
            MedicionDTO dtoPh4 = new MedicionDTO(null, 4.0, 22.5, null, 1L, 1L, 1L);
            Medicion m = new Medicion(1L, 4.0, 22.5, LocalDateTime.now(), 1L, 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(m);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());
            doNothing().when(notificacionUseCase).notificarAlerta(any(), eq("ALTA"));

            service.registrar(dtoPh4);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));
        }

        @Test
        @DisplayName("Debe notificar alerta MEDIA cuando el pH está en rango de advertencia (5.0–6.0)")
        void debeNotificarAlertaMediaSiPhEnAdvertencia() {
            MedicionDTO dtoPh55 = new MedicionDTO(null, 5.5, 22.5, null, 1L, 1L, 1L);
            Medicion m = new Medicion(1L, 5.5, 22.5, LocalDateTime.now(), 1L, 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(m);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());
            doNothing().when(notificacionUseCase).notificarAlerta(any(), eq("MEDIA"));

            service.registrar(dtoPh55);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));
        }

        @Test
        @DisplayName("Debe notificar alerta ALTA cuando la temperatura supera 35°C")
        void debeNotificarAlertaAltaSiTempCritica() {
            MedicionDTO dtoTemp40 = new MedicionDTO(null, 7.0, 40.0, null, 1L, 1L, 1L);
            Medicion m = new Medicion(1L, 7.0, 40.0, LocalDateTime.now(), 1L, 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(m);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());
            doNothing().when(notificacionUseCase).notificarAlerta(any(), eq("ALTA"));

            service.registrar(dtoTemp40);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));
        }

        @Test
        @DisplayName("Debe notificar alerta MEDIA cuando la temperatura está entre 30 y 35°C")
        void debeNotificarAlertaMediaSiTempElevada() {
            MedicionDTO dtoTemp32 = new MedicionDTO(null, 7.0, 32.0, null, 1L, 1L, 1L);
            Medicion m = new Medicion(1L, 7.0, 32.0, LocalDateTime.now(), 1L, 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(m);
            mockEnrich();
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());
            doNothing().when(notificacionUseCase).notificarAlerta(any(), eq("MEDIA"));

            service.registrar(dtoTemp32);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));
        }

        // ── Validaciones ──────────────────────────────────────────────

        @Test
        @DisplayName("Debe lanzar excepción si el DTO es nulo")
        void debeLanzarExcepcionSiDtoEsNulo() {
            assertThatThrownBy(() -> service.registrar(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La medición no puede ser nula");
        }

        @Test
        @DisplayName("Debe lanzar excepción si pH es nulo")
        void debeLanzarExcepcionSiPhEsNulo() {
            dtoValido.setPh(null);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("El pH es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si pH < 0")
        void debeLanzarExcepcionSiPhMenorACero() {
            dtoValido.setPh(-0.1);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("El pH debe estar entre 0 y 14");
        }

        @Test
        @DisplayName("Debe lanzar excepción si pH > 14")
        void debeLanzarExcepcionSiPhMayorA14() {
            dtoValido.setPh(14.1);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("El pH debe estar entre 0 y 14");
        }

        @Test
        @DisplayName("Debe lanzar excepción si temperatura es nula")
        void debeLanzarExcepcionSiTempEsNula() {
            dtoValido.setTemperatura(null);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("La temperatura es obligatoria");
        }

        @Test
        @DisplayName("Debe lanzar excepción si temperatura fuera de rango realista")
        void debeLanzarExcepcionSiTempFueraDeRango() {
            dtoValido.setTemperatura(101.0);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Temperatura fuera de rango realista");
        }

        @Test
        @DisplayName("Debe lanzar excepción si idRecurso es 0 o nulo")
        void debeLanzarExcepcionSiRecursoInvalido() {
            dtoValido.setIdRecurso(0L);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Debe seleccionar un recurso válido");
        }

        @Test
        @DisplayName("Debe lanzar excepción si idContaminante es nulo")
        void debeLanzarExcepcionSiContaminanteInvalido() {
            dtoValido.setIdContaminante(null);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Debe seleccionar un contaminante válido");
        }

        @Test
        @DisplayName("Debe lanzar excepción si idUsuario es negativo")
        void debeLanzarExcepcionSiUsuarioInvalido() {
            dtoValido.setIdUsuario(-1L);
            assertThatThrownBy(() -> service.registrar(dtoValido))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Usuario inválido");
        }

        @Test
        @DisplayName("Debe enriquecer con 'Desconocido' si el recurso no existe en BD")
        void debeEnriquecerConDesconocidoSiRecursoNoExiste() {
            when(repositoryPort.guardar(any())).thenReturn(entidadGuardada);
            when(recursoRepository.buscarPorId(1L)).thenReturn(Optional.empty());
            when(contaminanteRepository.buscarPorId(1L)).thenReturn(Optional.of(contaminante));
            doNothing().when(notificacionUseCase).notificarMedicion(any(), any(), anyDouble());

            MedicionDTO resultado = service.registrar(dtoValido);

            assertThat(resultado.getNombreRecurso()).isEqualTo("Desconocido");
        }
    }

    // ================================================================
    // BUSCAR POR ID
    // ================================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar la medición cuando el ID existe")
        void debeRetornarMedicion() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            mockEnrich();

            MedicionDTO resultado = service.buscarPorId(1L);
            assertThat(resultado.getPh()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
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
        @DisplayName("Debe retornar todas las mediciones")
        void debeRetornarTodas() {
            Medicion m2 = new Medicion(2L, 6.5, 18.0, LocalDateTime.now(), 2L, 1L, 1L);
            when(repositoryPort.listarTodos()).thenReturn(List.of(entidadGuardada, m2));
            mockEnrich();

            assertThat(service.listarTodos()).hasSize(2);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay datos")
        void debeRetornarListaVacia() {
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
        @DisplayName("Debe retornar mediciones filtradas por recurso")
        void debeRetornarPorRecurso() {
            when(repositoryPort.buscarPorRecurso(1L)).thenReturn(List.of(entidadGuardada));
            mockEnrich();

            assertThat(service.listarPorRecurso(1L)).hasSize(1);
        }
    }

    // ================================================================
    // ACTUALIZAR
    // ================================================================
    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar campos y re-evaluar alertas")
        void debeActualizar() {
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(entidadGuardada));
            Medicion actualizada = new Medicion(1L, 7.5, 24.0, LocalDateTime.now(), 1L, 1L, 1L);
            when(repositoryPort.guardar(any())).thenReturn(actualizada);
            mockEnrich();

            MedicionDTO resultado = service.actualizar(1L, new MedicionDTO(null, 7.5, 24.0, null, 1L, 1L, 1L));

            assertThat(resultado.getPh()).isEqualTo(7.5);
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
