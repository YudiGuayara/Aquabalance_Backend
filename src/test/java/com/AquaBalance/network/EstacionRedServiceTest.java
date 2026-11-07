package com.AquaBalance.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EstacionRedService - estado de la conexión")
class EstacionRedServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 7, 12, 0, 0);

    @Test
    @DisplayName("Sin ninguna comunicación el estado es SIN_DATOS")
    void sinDatos() {
        assertEquals(EstadoEstacion.SIN_DATOS,
                EstacionRedService.calcularEstado(null, null, null, null, AHORA));
    }

    @Test
    @DisplayName("Comunicación reciente y buena calidad es EN_LINEA")
    void enLinea() {
        assertEquals(EstadoEstacion.EN_LINEA,
                EstacionRedService.calcularEstado(AHORA.minusSeconds(5), 120, -70, 80, AHORA));
    }

    @Test
    @DisplayName("Más de 45 segundos sin comunicar es FUERA_DE_LINEA")
    void fueraDeLinea() {
        assertEquals(EstadoEstacion.FUERA_DE_LINEA,

                EstacionRedService.calcularEstado(AHORA.minusSeconds(46), 120, -70, 80, AHORA));
    }

    @Test
    @DisplayName("Exactamente 45 segundos todavía es EN_LINEA")
    void limiteDelTiempoSinComunicar() {
        assertEquals(EstadoEstacion.EN_LINEA,
                EstacionRedService.calcularEstado(AHORA.minusSeconds(45), 120, -70, 80, AHORA));
    }

    @Test
    @DisplayName("Latencia mayor a 800 ms es DEGRADADA")
    void degradadaPorLatencia() {
        assertEquals(EstadoEstacion.DEGRADADA,
                EstacionRedService.calcularEstado(AHORA.minusSeconds(5), 900, -70, 80, AHORA));
    }

    @Test
    @DisplayName("Señal más débil que -100 dBm es DEGRADADA")
    void degradadaPorSenal() {
        assertEquals(EstadoEstacion.DEGRADADA,
                EstacionRedService.calcularEstado(AHORA.minusSeconds(5), 120, -105, 80, AHORA));
    }

    @Test
    @DisplayName("Batería menor a 15% es DEGRADADA")
    void degradadaPorBateria() {
        assertEquals(EstadoEstacion.DEGRADADA,
                EstacionRedService.calcularEstado(AHORA.minusSeconds(5), 120, -70, 10, AHORA));
    }
}