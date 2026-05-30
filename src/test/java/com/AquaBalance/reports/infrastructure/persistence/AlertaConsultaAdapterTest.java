package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.AlertaRepository;
import com.AquaBalance.events.domain.NivelAlerta;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaConsultaAdapter - Pruebas Unitarias")
class AlertaConsultaAdapterTest {

    @Mock  private AlertaRepository alertaRepository;
    @InjectMocks private AlertaConsultaAdapter adapter;

    private Alerta alertaRoja;
    private Alerta alertaAmarilla;
    private Alerta alertaSinEvento;

    @BeforeEach
    void setUp() {
        alertaRoja     = new Alerta(1L, LocalDateTime.now(), NivelAlerta.Roja,    "Crítico",    1L, 10L);
        alertaAmarilla = new Alerta(2L, LocalDateTime.now(), NivelAlerta.Amarilla, "Advertencia", 1L, 10L);
        alertaSinEvento = new Alerta(3L, LocalDateTime.now(), NivelAlerta.Verde,   "Normal",     1L, null);
    }

    @Nested
    @DisplayName("contarPorEventos()")
    class ContarPorEventos {

        @Test
        @DisplayName("Debe contar solo las alertas cuyo idEvento esté en la lista")
        void debeContarCorrectamente() {
            when(alertaRepository.listarTodos()).thenReturn(List.of(alertaRoja, alertaAmarilla, alertaSinEvento));

            long count = adapter.contarPorEventos(List.of(10L));

            // alertaRoja e alertaAmarilla tienen idEvento=10, alertaSinEvento tiene null
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debe retornar cero si ningún evento coincide")
        void debeRetornarCeroSiNoCoincide() {
            when(alertaRepository.listarTodos()).thenReturn(List.of(alertaRoja, alertaAmarilla));

            assertThat(adapter.contarPorEventos(List.of(99L))).isZero();
        }

        @Test
        @DisplayName("Debe retornar cero si la lista de eventos está vacía")
        void debeRetornarCeroSiListaVacia() {
            when(alertaRepository.listarTodos()).thenReturn(List.of(alertaRoja));

            assertThat(adapter.contarPorEventos(List.of())).isZero();
        }
    }

    @Nested
    @DisplayName("alertasPorNivel()")
    class AlertasPorNivel {

        @Test
        @DisplayName("Debe agrupar alertas por nombre del nivel")
        void debeAgruparPorNivel() {
            when(alertaRepository.listarTodos()).thenReturn(List.of(alertaRoja, alertaAmarilla));

            Map<String, Long> result = adapter.alertasPorNivel(List.of(10L));

            assertThat(result).containsEntry("Roja", 1L);
            assertThat(result).containsEntry("Amarilla", 1L);
        }

        @Test
        @DisplayName("Debe usar 'SIN NIVEL' cuando el nivel es null")
        void debeUsarSinNivelCuandoEsNull() {
            Alerta sinNivel = new Alerta(4L, LocalDateTime.now(), null, "Sin nivel", 1L, 10L);
            when(alertaRepository.listarTodos()).thenReturn(List.of(sinNivel));

            Map<String, Long> result = adapter.alertasPorNivel(List.of(10L));

            assertThat(result).containsEntry("SIN NIVEL", 1L);
        }
    }

    @Nested
    @DisplayName("listarPorEventos()")
    class ListarPorEventos {

        @Test
        @DisplayName("Debe retornar AlertaResumen con los campos correctos")
        void debeRetornarResumenes() {
            when(alertaRepository.listarTodos()).thenReturn(List.of(alertaRoja));

            List<Estadisticas.AlertaResumen> result = adapter.listarPorEventos(List.of(10L));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getNivel()).isEqualTo("Roja");
            assertThat(result.get(0).getMensaje()).isEqualTo("Crítico");
        }

        @Test
        @DisplayName("Debe usar 'SIN NIVEL' cuando el nivel es null en el resumen")
        void debeUsarSinNivelEnResumen() {
            Alerta sinNivel = new Alerta(5L, LocalDateTime.now(), null, "Sin nivel", 1L, 10L);
            when(alertaRepository.listarTodos()).thenReturn(List.of(sinNivel));

            List<Estadisticas.AlertaResumen> result = adapter.listarPorEventos(List.of(10L));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNivel()).isEqualTo("SIN NIVEL");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay alertas en los eventos")
        void debeRetornarVacio() {
            when(alertaRepository.listarTodos()).thenReturn(List.of());

            assertThat(adapter.listarPorEventos(List.of(10L))).isEmpty();
        }
    }
}
