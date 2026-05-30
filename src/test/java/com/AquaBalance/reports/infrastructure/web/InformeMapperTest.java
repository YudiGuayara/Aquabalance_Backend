package com.AquaBalance.reports.infrastructure.web;

import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase.InformeDetalle;
import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase.InformeResumen;
import com.AquaBalance.reports.domain.Estadisticas;
import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.reports.infrastructure.web.dto.EstadisticasResponse;
import com.AquaBalance.reports.infrastructure.web.dto.InformeRequest;
import com.AquaBalance.reports.infrastructure.web.dto.InformeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InformeMapper - Pruebas Unitarias")
class InformeMapperTest {

    private Informe informe;

    @BeforeEach
    void setUp() {
        informe = new Informe();
        informe.setId(1L);
        informe.setTitulo("Informe Mayo");
        informe.setDescripcion("Análisis mensual");
        informe.setFechaGeneracion(LocalDateTime.of(2025, 5, 1, 10, 0));
        informe.setFechaInicio(LocalDateTime.of(2025, 4, 1, 0, 0));
        informe.setFechaFin(LocalDateTime.of(2025, 4, 30, 23, 59));
        informe.setRecursoId(1L);
        informe.setContaminanteId(2L);
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomain {

        @Test
        @DisplayName("Debe mapear correctamente todos los campos del request al dominio")
        void debeMappearRequest() {
            InformeRequest req = new InformeRequest();
            req.setTitulo("Informe");
            req.setDescripcion("Desc");
            req.setFechaInicio(LocalDateTime.of(2025, 1, 1, 0, 0));
            req.setFechaFin(LocalDateTime.of(2025, 1, 31, 23, 59));
            req.setRecursoId(10L);
            req.setContaminanteId(20L);

            Informe result = InformeMapper.toDomain(req);

            assertThat(result.getTitulo()).isEqualTo("Informe");
            assertThat(result.getDescripcion()).isEqualTo("Desc");
            assertThat(result.getFechaInicio()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
            assertThat(result.getFechaFin()).isEqualTo(LocalDateTime.of(2025, 1, 31, 23, 59));
            assertThat(result.getRecursoId()).isEqualTo(10L);
            assertThat(result.getContaminanteId()).isEqualTo(20L);
        }
    }

    @Nested
    @DisplayName("fromInforme()")
    class FromInforme {

        @Test
        @DisplayName("Debe mapear Informe → InformeResponse con campos básicos")
        void debeMappearInforme() {
            InformeResponse res = InformeMapper.fromInforme(informe);

            assertThat(res.getId()).isEqualTo(1L);
            assertThat(res.getTitulo()).isEqualTo("Informe Mayo");
            assertThat(res.getDescripcion()).isEqualTo("Análisis mensual");
            assertThat(res.getRecursoId()).isEqualTo(1L);
            assertThat(res.getContaminanteId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("No debe incluir estadísticas (null) en el resultado básico")
        void noDebeIncluirEstadisticas() {
            InformeResponse res = InformeMapper.fromInforme(informe);
            assertThat(res.getEstadisticas()).isNull();
        }
    }

    @Nested
    @DisplayName("fromResumen()")
    class FromResumen {

        @Test
        @DisplayName("Debe incluir nombres de recurso y contaminante")
        void debeIncluirNombres() {
            InformeResumen resumen = new InformeResumen(informe, "Río Bogotá", "Mercurio");
            InformeResponse res = InformeMapper.fromResumen(resumen);

            assertThat(res.getNombreRecurso()).isEqualTo("Río Bogotá");
            assertThat(res.getNombreContaminante()).isEqualTo("Mercurio");
            assertThat(res.getId()).isEqualTo(1L);
            assertThat(res.getTitulo()).isEqualTo("Informe Mayo");
        }
    }

    @Nested
    @DisplayName("fromDetalle()")
    class FromDetalle {

        @Test
        @DisplayName("Debe incluir estadísticas en el detalle")
        void debeIncluirEstadisticas() {
            Estadisticas stats = new Estadisticas();
            stats.setTotalMediciones(50L);
            stats.setPromedioPh(7.2);
            stats.setPromedioTemperatura(22.0);

            InformeDetalle detalle = new InformeDetalle(informe, "Río Bogotá", "Mercurio", stats);
            InformeResponse res = InformeMapper.fromDetalle(detalle);

            assertThat(res.getEstadisticas()).isNotNull();
            assertThat(res.getEstadisticas().getTotalMediciones()).isEqualTo(50L);
            assertThat(res.getNombreRecurso()).isEqualTo("Río Bogotá");
        }
    }

    @Nested
    @DisplayName("toEstadisticasResponse()")
    class ToEstadisticasResponse {

        @Test
        @DisplayName("Debe retornar null si estadísticas son null")
        void debeRetornarNullSiNull() {
            assertThat(InformeMapper.toEstadisticasResponse(null)).isNull();
        }

        @Test
        @DisplayName("Debe mapear todos los campos numéricos correctamente")
        void debeMappearCamposNumericos() {
            Estadisticas e = new Estadisticas();
            e.setTotalMediciones(100L);
            e.setPromedioPh(7.5);
            e.setPromedioTemperatura(20.0);
            e.setPhMinimo(6.0);
            e.setPhMaximo(9.0);
            e.setTemperaturaMinima(15.0);
            e.setTemperaturaMaxima(30.0);
            e.setTotalAlertas(3L);
            e.setTotalEventos(2L);
            e.setMedicionesPorContaminante(Map.of("Mercurio", 50L));
            e.setAlertasPorNivel(Map.of("ALTA", 2L));
            e.setEventosPorMagnitud(Map.of("Alta", 1L));

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getTotalMediciones()).isEqualTo(100L);
            assertThat(res.getPromedioPh()).isEqualTo(7.5);
            assertThat(res.getPhMinimo()).isEqualTo(6.0);
            assertThat(res.getPhMaximo()).isEqualTo(9.0);
            assertThat(res.getTemperaturaMinima()).isEqualTo(15.0);
            assertThat(res.getTemperaturaMaxima()).isEqualTo(30.0);
            assertThat(res.getTotalAlertas()).isEqualTo(3L);
            assertThat(res.getTotalEventos()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debe mapear puntos temporales de evolución de pH")
        void debeMappearEvolucionPh() {
            Estadisticas e = new Estadisticas();
            e.setEvolucionPh(List.of(
                    new Estadisticas.PuntoTemporal("01/05 08:00", 7.1),
                    new Estadisticas.PuntoTemporal("01/05 12:00", 7.3)
            ));

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getEvolucionPh()).hasSize(2);
            assertThat(res.getEvolucionPh().get(0).getFecha()).isEqualTo("01/05 08:00");
            assertThat(res.getEvolucionPh().get(1).getValor()).isEqualTo(7.3);
        }

        @Test
        @DisplayName("Debe mapear puntos temporales de evolución de temperatura")
        void debeMappearEvolucionTemperatura() {
            Estadisticas e = new Estadisticas();
            e.setEvolucionTemperatura(List.of(
                    new Estadisticas.PuntoTemporal("01/05 08:00", 20.0)
            ));

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getEvolucionTemperatura()).hasSize(1);
        }

        @Test
        @DisplayName("Debe mapear alertas resumidas correctamente")
        void debeMappearAlertas() {
            Estadisticas e = new Estadisticas();
            e.setAlertas(List.of(
                    new Estadisticas.AlertaResumen(1L, "ALTA", "Mercurio crítico",
                            LocalDateTime.of(2025, 4, 15, 10, 0))
            ));

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getAlertas()).hasSize(1);
            assertThat(res.getAlertas().get(0).getNivel()).isEqualTo("ALTA");
            assertThat(res.getAlertas().get(0).getMensaje()).isEqualTo("Mercurio crítico");
        }

        @Test
        @DisplayName("Debe mapear eventos resumidos correctamente")
        void debeMappearEventos() {
            Estadisticas e = new Estadisticas();
            e.setEventos(List.of(
                    new Estadisticas.EventoResumen(1L, "Desbordamiento", "Alta",
                            LocalDateTime.of(2025, 4, 10, 9, 0))
            ));

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getEventos()).hasSize(1);
            assertThat(res.getEventos().get(0).getDescripcion()).isEqualTo("Desbordamiento");
        }

        @Test
        @DisplayName("Debe manejar listas null en evolución sin lanzar excepción")
        void debeManejarEvolucionNula() {
            Estadisticas e = new Estadisticas();
            e.setEvolucionPh(null);
            e.setEvolucionTemperatura(null);
            e.setAlertas(null);
            e.setEventos(null);

            EstadisticasResponse res = InformeMapper.toEstadisticasResponse(e);

            assertThat(res.getEvolucionPh()).isNull();
            assertThat(res.getEvolucionTemperatura()).isNull();
            assertThat(res.getAlertas()).isNull();
            assertThat(res.getEventos()).isNull();
        }
    }
}
