package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.ContaminanteRepository;
import com.AquaBalance.monitoring.domain.Medicion;
import com.AquaBalance.monitoring.domain.MedicionRepository;
import com.AquaBalance.monitoring.domain.NivelContaminante;
import com.AquaBalance.reports.domain.Estadisticas;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicionConsultaAdapter - Pruebas Unitarias")
class MedicionConsultaAdapterTest {

    @Mock private MedicionRepository     medicionRepository;
    @Mock private ContaminanteRepository contaminanteRepository;
    @InjectMocks private MedicionConsultaAdapter adapter;

    // Rango de prueba compartido
    private static final Long RID = 1L;
    private static final Long CID = 1L;
    private static final LocalDateTime INI = LocalDateTime.of(2025, 4,  1,  0,  0);
    private static final LocalDateTime FIN = LocalDateTime.of(2025, 4, 30, 23, 59);

    private Medicion m1; // recurso=1, contaminante=1, dentro del rango
    private Medicion m2; // recurso=1, contaminante=1, dentro del rango
    private Medicion m3; // recurso=2, contaminante=2 → fuera de filtro
    private Medicion m4; // recurso=1, contaminante=1, FUERA del rango de fechas

    @BeforeEach
    void setUp() {
        m1 = new Medicion(1L, 7.0, 22.0, LocalDateTime.of(2025, 4,  5, 8, 0), 1L, 1L, 1L);
        m2 = new Medicion(2L, 8.0, 25.0, LocalDateTime.of(2025, 4, 15, 8, 0), 1L, 1L, 1L);
        m3 = new Medicion(3L, 6.5, 18.0, LocalDateTime.of(2025, 4, 10, 8, 0), 2L, 2L, 2L);
        m4 = new Medicion(4L, 7.5, 20.0, LocalDateTime.of(2025, 6,  1, 8, 0), 1L, 1L, 1L);
    }

    // ────────────────────────────────────────────────────────────────
    // contarPorRecursoContaminanteYPeriodo
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("contarPorRecursoContaminanteYPeriodo()")
    class Contar {

        @Test
        @DisplayName("Debe contar solo las mediciones que coinciden con recurso, contaminante y período")
        void debeContarCorrectamente() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2, m3, m4));

            long count = adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN);

            assertThat(count).isEqualTo(2L); // solo m1 y m2 pasan el filtro
        }

        @Test
        @DisplayName("Debe retornar cero si no hay mediciones en el período")
        void debeRetornarCeroSiNada() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m3, m4));

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isZero();
        }

        @Test
        @DisplayName("Debe retornar cero con lista vacía")
        void debeRetornarCeroConListaVacia() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isZero();
        }
    }

    // ────────────────────────────────────────────────────────────────
    // promedioPhPorPeriodo
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("promedioPhPorPeriodo()")
    class PromedioPh {

        @Test
        @DisplayName("Debe calcular el promedio de pH correctamente")
        void debeCalcularPromedioPh() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            // (7.0 + 8.0) / 2 = 7.5
            Double result = adapter.promedioPhPorPeriodo(RID, CID, INI, FIN);

            assertThat(result).isEqualTo(7.5);
        }

        @Test
        @DisplayName("Debe retornar 0.0 si no hay mediciones")
        void debeRetornarCeroSiNoHay() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.promedioPhPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // promedioTemperaturaPorPeriodo
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("promedioTemperaturaPorPeriodo()")
    class PromedioTemperatura {

        @Test
        @DisplayName("Debe calcular el promedio de temperatura correctamente")
        void debeCalcularPromedioTemperatura() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            // (22.0 + 25.0) / 2 = 23.5
            Double result = adapter.promedioTemperaturaPorPeriodo(RID, CID, INI, FIN);

            assertThat(result).isEqualTo(23.5);
        }

        @Test
        @DisplayName("Debe retornar 0.0 si no hay mediciones")
        void debeRetornarCero() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.promedioTemperaturaPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // minimoPhPorPeriodo / maximoPhPorPeriodo
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("minimoPhPorPeriodo() y maximoPhPorPeriodo()")
    class PhMinMax {

        @Test
        @DisplayName("Debe retornar el pH mínimo del período")
        void debeRetornarPhMinimo() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            assertThat(adapter.minimoPhPorPeriodo(RID, CID, INI, FIN)).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Debe retornar el pH máximo del período")
        void debeRetornarPhMaximo() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            assertThat(adapter.maximoPhPorPeriodo(RID, CID, INI, FIN)).isEqualTo(8.0);
        }

        @Test
        @DisplayName("minimoPhPorPeriodo debe retornar 0.0 si no hay datos")
        void minimoPhCeroSiNoHay() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.minimoPhPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("maximoPhPorPeriodo debe retornar 0.0 si no hay datos")
        void maximoPhCeroSiNoHay() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.maximoPhPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // minimoTemperaturaPorPeriodo / maximoTemperaturaPorPeriodo
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("minimoTemperaturaPorPeriodo() y maximoTemperaturaPorPeriodo()")
    class TempMinMax {

        @Test
        @DisplayName("Debe retornar la temperatura mínima del período")
        void debeRetornarTempMinima() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            assertThat(adapter.minimoTemperaturaPorPeriodo(RID, CID, INI, FIN)).isEqualTo(22.0);
        }

        @Test
        @DisplayName("Debe retornar la temperatura máxima del período")
        void debeRetornarTempMaxima() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            assertThat(adapter.maximoTemperaturaPorPeriodo(RID, CID, INI, FIN)).isEqualTo(25.0);
        }

        @Test
        @DisplayName("minimoTemperaturaPorPeriodo debe retornar 0.0 si no hay datos")
        void minimoTempCeroSiNoHay() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.minimoTemperaturaPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("maximoTemperaturaPorPeriodo debe retornar 0.0 si no hay datos")
        void maximoTempCeroSiNoHay() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.maximoTemperaturaPorPeriodo(RID, CID, INI, FIN)).isEqualTo(0.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // medicionesPorContaminante
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("medicionesPorContaminante()")
    class MedicionesPorContaminante {

        @Test
        @DisplayName("Debe agrupar mediciones por nombre del contaminante")
        void debeAgruparPorNombre() {
            Contaminante mercurio = new Contaminante(1L, "Mercurio", 5.0, NivelContaminante.Alto, "Minería");
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));
            when(contaminanteRepository.buscarPorId(1L)).thenReturn(Optional.of(mercurio));

            Map<String, Long> result = adapter.medicionesPorContaminante(RID, CID, INI, FIN);

            assertThat(result).containsEntry("Mercurio", 2L);
        }

        @Test
        @DisplayName("Debe usar 'Desconocido' si el contaminante no se encuentra en repositorio")
        void debeUsarDesconocido() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1));
            when(contaminanteRepository.buscarPorId(1L)).thenReturn(Optional.empty());

            Map<String, Long> result = adapter.medicionesPorContaminante(RID, CID, INI, FIN);

            assertThat(result).containsEntry("Desconocido", 1L);
        }

        @Test
        @DisplayName("Debe retornar mapa vacío si no hay mediciones en el período")
        void debeRetornarMapaVacio() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.medicionesPorContaminante(RID, CID, INI, FIN)).isEmpty();
        }
    }

    // ────────────────────────────────────────────────────────────────
    // evolucionPh
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("evolucionPh()")
    class EvolucionPh {

        @Test
        @DisplayName("Debe retornar puntos temporales con el pH promedio por momento")
        void debeRetornarPuntosTemporales() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            List<Estadisticas.PuntoTemporal> resultado = adapter.evolucionPh(RID, CID, INI, FIN);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getValor()).isEqualTo(7.0);
            assertThat(resultado.get(1).getValor()).isEqualTo(8.0);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay mediciones")
        void debeRetornarVacio() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.evolucionPh(RID, CID, INI, FIN)).isEmpty();
        }

        @Test
        @DisplayName("Debe ordenar los puntos cronológicamente")
        void debeOrdenarCronologicamente() {
            // m2 tiene fecha 15/04 y m1 tiene 05/04; se pasan en orden inverso
            when(medicionRepository.listarTodos()).thenReturn(List.of(m2, m1));

            List<Estadisticas.PuntoTemporal> resultado = adapter.evolucionPh(RID, CID, INI, FIN);

            assertThat(resultado.get(0).getValor()).isEqualTo(7.0); // m1 primero cronológicamente
            assertThat(resultado.get(1).getValor()).isEqualTo(8.0); // m2 segundo
        }
    }

    // ────────────────────────────────────────────────────────────────
    // evolucionTemperatura
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("evolucionTemperatura()")
    class EvolucionTemperatura {

        @Test
        @DisplayName("Debe retornar puntos temporales con temperatura promedio por momento")
        void debeRetornarPuntosTemperatura() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, m2));

            List<Estadisticas.PuntoTemporal> resultado = adapter.evolucionTemperatura(RID, CID, INI, FIN);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getValor()).isEqualTo(22.0);
            assertThat(resultado.get(1).getValor()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay mediciones")
        void debeRetornarVacio() {
            when(medicionRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.evolucionTemperatura(RID, CID, INI, FIN)).isEmpty();
        }

        @Test
        @DisplayName("Debe agrupar mediciones con la misma fecha en un único punto promediado")
        void debeAgruparMismaMarca() {
            // Dos mediciones en exactamente el mismo timestamp → deben dar un único punto
            Medicion dup = new Medicion(5L, 9.0, 30.0, m1.getFecha(), 1L, 1L, 1L);
            when(medicionRepository.listarTodos()).thenReturn(List.of(m1, dup));

            List<Estadisticas.PuntoTemporal> resultado = adapter.evolucionTemperatura(RID, CID, INI, FIN);

            // (22.0 + 30.0) / 2 = 26.0
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getValor()).isEqualTo(26.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // filtro interno — mediciones fuera del rango de fechas
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Filtrado por período")
    class FiltroPeriodo {

        @Test
        @DisplayName("No debe incluir mediciones anteriores al inicio del período")
        void noDebeIncluirAntesDeInicio() {
            Medicion anterior = new Medicion(6L, 7.0, 22.0,
                    LocalDateTime.of(2025, 3, 31, 23, 59), 1L, 1L, 1L);
            when(medicionRepository.listarTodos()).thenReturn(List.of(anterior));

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isZero();
        }

        @Test
        @DisplayName("No debe incluir mediciones posteriores al fin del período")
        void noDebeIncluirDespuesDeFin() {
            when(medicionRepository.listarTodos()).thenReturn(List.of(m4)); // m4 es junio

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isZero();
        }

        @Test
        @DisplayName("Debe incluir mediciones exactamente en los límites del período")
        void debeIncluirLimites() {
            Medicion enInicio = new Medicion(7L, 7.0, 22.0, INI, 1L, 1L, 1L);
            Medicion enFin    = new Medicion(8L, 8.0, 25.0, FIN, 1L, 1L, 1L);
            when(medicionRepository.listarTodos()).thenReturn(List.of(enInicio, enFin));

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isEqualTo(2L);
        }

        @Test
        @DisplayName("No debe incluir mediciones con fecha null")
        void noDebeIncluirFechaNula() {
            Medicion sinFecha = new Medicion(9L, 7.0, 22.0, null, 1L, 1L, 1L);
            when(medicionRepository.listarTodos()).thenReturn(List.of(sinFecha));

            assertThat(adapter.contarPorRecursoContaminanteYPeriodo(RID, CID, INI, FIN)).isZero();
        }
    }
}
