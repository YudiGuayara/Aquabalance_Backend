package com.AquaBalance.shared.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitService - Pruebas Unitarias")
class RateLimitServiceTest {

    private final RateLimitService service = new RateLimitService();

    @Test
    @DisplayName("Permite hasta el máximo y bloquea el siguiente intento")
    void bloqueaDespuesDelMaximo() {
        Duration ventana = Duration.ofMinutes(5);

        assertThat(service.permitir("k", 3, ventana)).isTrue();
        assertThat(service.permitir("k", 3, ventana)).isTrue();
        assertThat(service.permitir("k", 3, ventana)).isTrue();
        assertThat(service.permitir("k", 3, ventana)).isFalse();
        assertThat(service.bloqueado("k", 3, ventana)).isTrue();
    }

    @Test
    @DisplayName("limpiar() reinicia el contador de la clave")
    void limpiarReiniciaContador() {
        Duration ventana = Duration.ofMinutes(5);
        service.permitir("k", 1, ventana);
        assertThat(service.bloqueado("k", 1, ventana)).isTrue();

        service.limpiar("k");

        assertThat(service.bloqueado("k", 1, ventana)).isFalse();
    }

    @Test
    @DisplayName("Las claves son independientes entre sí")
    void clavesIndependientes() {
        Duration ventana = Duration.ofMinutes(5);
        service.permitir("a", 1, ventana);

        assertThat(service.bloqueado("a", 1, ventana)).isTrue();
        assertThat(service.bloqueado("b", 1, ventana)).isFalse();
    }
}
