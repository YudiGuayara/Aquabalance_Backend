package com.AquaBalance.reports.application;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase;
import com.AquaBalance.reports.application.ports.out.*;
import com.AquaBalance.reports.domain.Estadisticas;
import com.AquaBalance.reports.domain.Informe;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InformeService - Pruebas Unitarias")
class InformeServiceTest {

    @Mock private InformeRepositoryPort       informePort;
    @Mock private RecursoConsultaPort         recursoPort;
    @Mock private ContaminanteConsultaPort    contaminantePort;
    @Mock private MedicionConsultaPort        medicionPort;
    @Mock private EventoConsultaPort          eventoPort;
    @Mock private AlertaConsultaPort          alertaPort;
    @Mock private GestionarNotificacionUseCase notificacionUseCase;

    @InjectMocks
    private InformeService service;

    private static final LocalDateTime INICIO = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final LocalDateTime FIN    = LocalDateTime.of(2025, 3, 31, 23, 59);

    private Informe informeValido;

    @BeforeEach
    void setUp() {
        informeValido = new Informe();
        informeValido.setTitulo("Informe Q1 2025");
        informeValido.setDescripcion("Análisis trimestral");
        informeValido.setFechaInicio(INICIO);
        informeValido.setFechaFin(FIN);
        informeValido.setRecursoId(1L);
        informeValido.setContaminanteId(1L);
    }

    /** Configura todos los mocks de estadísticas con valores dentro de rangos seguros */
    private void mockEstadisticasNormales() {
        when(medicionPort.contarPorRecursoContaminanteYPeriodo(any(), any(), any(), any())).thenReturn(10L);
        when(medicionPort.promedioPhPorPeriodo(any(), any(), any(), any())).thenReturn(7.2);
        when(medicionPort.promedioTemperaturaPorPeriodo(any(), any(), any(), any())).thenReturn(21.0);
        when(medicionPort.minimoPhPorPeriodo(any(), any(), any(), any())).thenReturn(6.8);   // > 5.0 → sin alerta
        when(medicionPort.maximoPhPorPeriodo(any(), any(), any(), any())).thenReturn(8.1);   // < 9.0 → sin alerta
        when(medicionPort.minimoTemperaturaPorPeriodo(any(), any(), any(), any())).thenReturn(18.0);
        when(medicionPort.maximoTemperaturaPorPeriodo(any(), any(), any(), any())).thenReturn(25.0); // < 35 → sin alerta
        when(medicionPort.medicionesPorContaminante(any(), any(), any(), any())).thenReturn(Map.of("Mercurio", 10L));
        when(medicionPort.evolucionPh(any(), any(), any(), any())).thenReturn(List.of());
        when(medicionPort.evolucionTemperatura(any(), any(), any(), any())).thenReturn(List.of());
        when(eventoPort.contarPorRecursoContaminanteYPeriodo(any(), any(), any(), any())).thenReturn(2L);
        when(eventoPort.eventosPorMagnitud(any(), any(), any(), any())).thenReturn(Map.of("Alta", 2L));
        when(eventoPort.listarPorRecursoContaminanteYPeriodo(any(), any(), any(), any())).thenReturn(List.of());
        when(eventoPort.idsEventosPorRecursoContaminanteYPeriodo(any(), any(), any(), any()))
                .thenReturn(List.of(10L));
        when(alertaPort.contarPorEventos(anyList())).thenReturn(2L);   // < 5 → sin alerta
        when(alertaPort.alertasPorNivel(anyList())).thenReturn(Map.of("Roja", 2L));
        when(alertaPort.listarPorEventos(anyList())).thenReturn(List.of());
    }

    // ================================================================
    // CREAR
    // ================================================================
    @Nested
    @DisplayName("crearInforme()")
    class CrearInforme {

        @Test
        @DisplayName("Debe crear informe, asignar fecha de generación y notificar")
        void debeCrearInformeYNotificar() {
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            when(informePort.save(any())).thenAnswer(inv -> {
                Informe inf = inv.getArgument(0);
                inf.setId(1L);
                return inf;
            });
            doNothing().when(notificacionUseCase).notificarInforme(anyString());

            Informe resultado = service.crearInforme(informeValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getFechaGeneracion()).isNotNull();
            verify(notificacionUseCase).notificarInforme("Informe Q1 2025");
        }

        @Test
        @DisplayName("Debe lanzar excepción si las fechas son nulas")
        void debeLanzarExcepcionSiFechasNulas() {
            informeValido.setFechaInicio(null);
            assertThatThrownBy(() -> service.crearInforme(informeValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Las fechas son obligatorias");
        }

        @Test
        @DisplayName("Debe lanzar excepción si fecha fin es anterior a fecha inicio")
        void debeLanzarExcepcionSiFechaFinAnterior() {
            informeValido.setFechaFin(INICIO.minusDays(1));
            assertThatThrownBy(() -> service.crearInforme(informeValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La fecha fin no puede ser menor a la fecha inicio");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el recurso no existe")
        void debeLanzarExcepcionSiRecursoNoExiste() {
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.crearInforme(informeValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Recurso no encontrado");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el contaminante no existe")
        void debeLanzarExcepcionSiContaminanteNoExiste() {
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.crearInforme(informeValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contaminante no encontrado");
        }

        @Test
        @DisplayName("Debe aceptar fecha inicio == fecha fin (mismo instante)")
        void debeAceptarFechasIguales() {
            informeValido.setFechaFin(INICIO);
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            when(informePort.save(any())).thenReturn(informeValido);
            doNothing().when(notificacionUseCase).notificarInforme(anyString());

            assertThatNoException().isThrownBy(() -> service.crearInforme(informeValido));
        }
    }

    // ================================================================
    // OBTENER CON ESTADÍSTICAS
    // ================================================================
    @Nested
    @DisplayName("obtenerInforme()")
    class ObtenerInforme {

        @Test
        @DisplayName("Debe retornar detalle completo con estadísticas y sin alertas en rangos normales")
        void debeRetornarDetalleConEstadisticas() {
            informeValido.setId(1L);
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            mockEstadisticasNormales();

            GestionarInformeUseCase.InformeDetalle detalle = service.obtenerInforme(1L);

            assertThat(detalle.getInforme().getId()).isEqualTo(1L);
            assertThat(detalle.getNombreRecurso()).isEqualTo("Río Bogotá");
            assertThat(detalle.getEstadisticas().getTotalMediciones()).isEqualTo(10L);
            assertThat(detalle.getEstadisticas().getTotalAlertas()).isEqualTo(2L);
            // Rangos normales → no disparar alerta adicional
            verify(notificacionUseCase, never()).notificarAlerta(any(), any());
        }

        @Test
        @DisplayName("Debe disparar alerta ALTA cuando pH mínimo es crítico (< 5.0)")
        void debeDispararAlertaAltaSiPhMinimoCritico() {
            informeValido.setId(1L);
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            mockEstadisticasNormales();
            // Sobreescribir pH mínimo a valor crítico
            when(medicionPort.minimoPhPorPeriodo(any(), any(), any(), any())).thenReturn(4.0);
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));

            service.obtenerInforme(1L);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("ALTA"));
        }

        @Test
        @DisplayName("Debe disparar alerta MEDIA cuando hay 5 o más alertas en el período")
        void debeDispararAlertaMediaConMuchasAlertas() {
            informeValido.setId(1L);
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            mockEstadisticasNormales();
            when(alertaPort.contarPorEventos(anyList())).thenReturn(5L); // exactamente 5 → dispara
            doNothing().when(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));

            service.obtenerInforme(1L);

            verify(notificacionUseCase).notificarAlerta(anyString(), eq("MEDIA"));
        }

        @Test
        @DisplayName("Debe retornar cero alertas si no hay eventos en el período")
        void debeRetornarCeroAlertasSiNoHayEventos() {
            informeValido.setId(1L);
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            // estadísticas con 0 eventos
            when(medicionPort.contarPorRecursoContaminanteYPeriodo(any(),any(),any(),any())).thenReturn(5L);
            when(medicionPort.promedioPhPorPeriodo(any(),any(),any(),any())).thenReturn(7.0);
            when(medicionPort.promedioTemperaturaPorPeriodo(any(),any(),any(),any())).thenReturn(20.0);
            when(medicionPort.minimoPhPorPeriodo(any(),any(),any(),any())).thenReturn(6.5);
            when(medicionPort.maximoPhPorPeriodo(any(),any(),any(),any())).thenReturn(7.5);
            when(medicionPort.minimoTemperaturaPorPeriodo(any(),any(),any(),any())).thenReturn(18.0);
            when(medicionPort.maximoTemperaturaPorPeriodo(any(),any(),any(),any())).thenReturn(24.0);
            when(medicionPort.medicionesPorContaminante(any(),any(),any(),any())).thenReturn(Map.of());
            when(medicionPort.evolucionPh(any(),any(),any(),any())).thenReturn(List.of());
            when(medicionPort.evolucionTemperatura(any(),any(),any(),any())).thenReturn(List.of());
            when(eventoPort.contarPorRecursoContaminanteYPeriodo(any(),any(),any(),any())).thenReturn(0L);
            when(eventoPort.eventosPorMagnitud(any(),any(),any(),any())).thenReturn(Map.of());
            when(eventoPort.listarPorRecursoContaminanteYPeriodo(any(),any(),any(),any())).thenReturn(List.of());
            when(eventoPort.idsEventosPorRecursoContaminanteYPeriodo(any(),any(),any(),any()))
                    .thenReturn(Collections.emptyList());

            GestionarInformeUseCase.InformeDetalle detalle = service.obtenerInforme(1L);

            assertThat(detalle.getEstadisticas().getTotalAlertas()).isEqualTo(0L);
            verifyNoInteractions(alertaPort);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el informe no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(informePort.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obtenerInforme(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // LISTAR
    // ================================================================
    @Nested
    @DisplayName("listarInformes()")
    class ListarInformes {

        @Test
        @DisplayName("Debe retornar resúmenes enriquecidos con nombres")
        void debeRetornarResumenes() {
            Informe i2 = new Informe();
            i2.setId(2L); i2.setTitulo("Q2"); i2.setRecursoId(2L); i2.setContaminanteId(2L);
            when(informePort.findAll()).thenReturn(List.of(informeValido, i2));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(recursoPort.buscarNombrePorId(2L)).thenReturn(Optional.of("Lago Calima"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            when(contaminantePort.buscarNombrePorId(2L)).thenReturn(Optional.of("Plomo"));

            List<GestionarInformeUseCase.InformeResumen> resultado = service.listarInformes();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getNombreRecurso()).isEqualTo("Río Bogotá");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay informes")
        void debeRetornarListaVacia() {
            when(informePort.findAll()).thenReturn(List.of());
            assertThat(service.listarInformes()).isEmpty();
        }
    }

    // ================================================================
    // ACTUALIZAR
    // ================================================================
    @Nested
    @DisplayName("actualizarInforme()")
    class ActualizarInforme {

        @Test
        @DisplayName("Debe actualizar campos y notificar")
        void debeActualizarYNotificar() {
            informeValido.setId(1L);
            informeValido.setTitulo("Informe Q1 2025");
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));
            when(recursoPort.buscarNombrePorId(1L)).thenReturn(Optional.of("Río Bogotá"));
            when(contaminantePort.buscarNombrePorId(1L)).thenReturn(Optional.of("Mercurio"));
            when(informePort.save(any())).thenReturn(informeValido);
            doNothing().when(notificacionUseCase).notificarInforme(anyString());

            Informe nuevo = new Informe();
            nuevo.setTitulo("Informe Actualizado"); nuevo.setDescripcion("Nueva desc");
            nuevo.setFechaInicio(INICIO); nuevo.setFechaFin(FIN);
            nuevo.setRecursoId(1L); nuevo.setContaminanteId(1L);

            Informe resultado = service.actualizarInforme(1L, nuevo);

            assertThat(resultado.getTitulo()).isEqualTo("Informe Actualizado");
            verify(notificacionUseCase).notificarInforme(anyString());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(informePort.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.actualizarInforme(99L, informeValido))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si las nuevas fechas son inválidas")
        void debeLanzarExcepcionSiFechasInvalidas() {
            informeValido.setId(1L);
            when(informePort.findById(1L)).thenReturn(Optional.of(informeValido));

            Informe invalido = new Informe();
            invalido.setFechaInicio(FIN); invalido.setFechaFin(INICIO); // invertidas
            invalido.setRecursoId(1L); invalido.setContaminanteId(1L);

            assertThatThrownBy(() -> service.actualizarInforme(1L, invalido))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ================================================================
    // ELIMINAR
    // ================================================================
    @Nested
    @DisplayName("eliminarInforme()")
    class EliminarInforme {

        @Test
        @DisplayName("Debe eliminar si existe")
        void debeEliminar() {
            when(informePort.existsById(1L)).thenReturn(true);
            doNothing().when(informePort).deleteById(1L);

            assertThatNoException().isThrownBy(() -> service.eliminarInforme(1L));
            verify(informePort).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no existe")
        void debeLanzarExcepcion() {
            when(informePort.existsById(99L)).thenReturn(false);
            assertThatThrownBy(() -> service.eliminarInforme(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(informePort, never()).deleteById(anyLong());
        }
    }
}
