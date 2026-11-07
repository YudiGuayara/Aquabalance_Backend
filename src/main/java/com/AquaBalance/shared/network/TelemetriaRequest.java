package com.AquaBalance.network;

/** Datos que envía una estación (sensor) al servidor en cada transmisión. */
public record TelemetriaRequest(Integer latenciaMs, Integer senalDbm, Integer bateriaPct) {
}