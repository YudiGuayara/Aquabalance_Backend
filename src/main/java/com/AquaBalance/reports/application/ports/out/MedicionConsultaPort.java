package com.AquaBalance.reports.application.ports.out;

import com.AquaBalance.reports.domain.Estadisticas;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MedicionConsultaPort {

    long contarPorRecursoContaminanteYPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double promedioPhPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double promedioTemperaturaPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double minimoPhPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double maximoPhPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double minimoTemperaturaPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Double maximoTemperaturaPorPeriodo(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    Map<String, Long> medicionesPorContaminante(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    List<Estadisticas.PuntoTemporal> evolucionPh(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );

    List<Estadisticas.PuntoTemporal> evolucionTemperatura(
            Long recursoId, Long contaminanteId,
            LocalDateTime inicio, LocalDateTime fin
    );
}