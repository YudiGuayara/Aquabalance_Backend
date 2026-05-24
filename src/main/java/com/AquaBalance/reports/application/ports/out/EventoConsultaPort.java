package com.AquaBalance.reports.application.ports.out;

import com.AquaBalance.reports.domain.Estadisticas;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventoConsultaPort {

    long contarPorRecursoContaminanteYPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Map<String, Long> eventosPorMagnitud(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    List<Estadisticas.EventoResumen> listarPorRecursoContaminanteYPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    List<Long> idsEventosPorRecursoContaminanteYPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );
}