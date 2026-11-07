package com.AquaBalance.network;

public record EstacionRedResponse(
        Long id,
        Long recursoId,
        String nombre,
        String tipoRecurso,
        String ip,
        ProtocoloRed protocolo,
        Integer puerto,
        EstadoEstacion estado,
        Integer latenciaMs,
        Integer senalDbm,
        Integer bateriaPct,
        Long segundosSinComunicacion,
        Long paquetesRecibidos,
        Long paquetesPerdidos,
        Double perdidaPct
) {
}