package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.Medicion;
import com.AquaBalance.monitoring.domain.MedicionRepository;
import com.AquaBalance.monitoring.domain.ContaminanteRepository;
import com.AquaBalance.reports.application.ports.out.MedicionConsultaPort;
import com.AquaBalance.reports.domain.Estadisticas;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MedicionConsultaAdapter implements MedicionConsultaPort {

    private final MedicionRepository     medicionRepository;
    private final ContaminanteRepository contaminanteRepository;

    public MedicionConsultaAdapter(MedicionRepository medicionRepository,
                                   ContaminanteRepository contaminanteRepository) {
        this.medicionRepository     = medicionRepository;
        this.contaminanteRepository = contaminanteRepository;
    }

    private List<Medicion> filtrar(Long recursoId, Long contaminanteId,
                                   LocalDateTime inicio, LocalDateTime fin) {

        List<Medicion> todas = medicionRepository.listarTodos();

        System.out.println("=== FILTRO MEDICIONES ===");
        System.out.println("Buscando recursoId=" + recursoId + " contaminanteId=" + contaminanteId);
        System.out.println("Período: " + inicio + " → " + fin);
        System.out.println("Total mediciones en BD: " + todas.size());

        todas.forEach(m -> System.out.println(
                "  ID=" + m.getId() +
                        " recursoId=" + m.getIdRecurso() +
                        " (tipo=" + (m.getIdRecurso() != null ? m.getIdRecurso().getClass().getSimpleName() : "null") + ")" +
                        " contaminanteId=" + m.getIdContaminante() +
                        " (tipo=" + (m.getIdContaminante() != null ? m.getIdContaminante().getClass().getSimpleName() : "null") + ")" +
                        " fecha=" + m.getFecha() +
                        " match_recurso=" + recursoId.equals(m.getIdRecurso()) +
                        " match_contaminante=" + contaminanteId.equals(m.getIdContaminante()) +
                        " match_fecha=" + (m.getFecha() != null &&
                        !m.getFecha().isBefore(inicio) &&
                        !m.getFecha().isAfter(fin))
        ));

        List<Medicion> resultado = todas.stream()
                .filter(m -> recursoId.equals(m.getIdRecurso()))
                .filter(m -> contaminanteId.equals(m.getIdContaminante()))
                .filter(m -> m.getFecha() != null)
                .filter(m -> !m.getFecha().isBefore(inicio) && !m.getFecha().isAfter(fin))
                .collect(Collectors.toList());

        System.out.println("Mediciones que pasaron el filtro: " + resultado.size());
        System.out.println("========================");

        return resultado;
    }

    @Override
    public long contarPorRecursoContaminanteYPeriodo(Long r, Long c,
                                                     LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).size();
    }

    @Override
    public Double promedioPhPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getPh).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    @Override
    public Double promedioTemperaturaPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getTemperatura).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    @Override
    public Double minimoPhPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getPh).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).min().orElse(0.0));
    }

    @Override
    public Double maximoPhPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getPh).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).max().orElse(0.0));
    }

    @Override
    public Double minimoTemperaturaPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getTemperatura).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).min().orElse(0.0));
    }

    @Override
    public Double maximoTemperaturaPorPeriodo(Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return redondear(filtrar(r, c, i, f).stream()
                .map(Medicion::getTemperatura).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).max().orElse(0.0));
    }

    @Override
    public Map<String, Long> medicionesPorContaminante(Long r, Long c,
                                                       LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).stream()
                .collect(Collectors.groupingBy(
                        m -> contaminanteRepository.buscarPorId(m.getIdContaminante())
                                .map(ct -> ct.getNombre()).orElse("Desconocido"),
                        Collectors.counting()
                ));
    }

    @Override
    public List<Estadisticas.PuntoTemporal> evolucionPh(Long r, Long c,
                                                        LocalDateTime i, LocalDateTime f) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return filtrar(r, c, i, f).stream()
                .sorted(Comparator.comparing(Medicion::getFecha))
                .collect(Collectors.groupingBy(
                        m -> m.getFecha().format(fmt),
                        LinkedHashMap::new,
                        Collectors.averagingDouble(Medicion::getPh)
                ))
                .entrySet().stream()
                .map(e -> new Estadisticas.PuntoTemporal(e.getKey(), redondear(e.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Estadisticas.PuntoTemporal> evolucionTemperatura(Long r, Long c,
                                                                 LocalDateTime i, LocalDateTime f) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return filtrar(r, c, i, f).stream()
                .sorted(Comparator.comparing(Medicion::getFecha))
                .collect(Collectors.groupingBy(
                        m -> m.getFecha().format(fmt),
                        LinkedHashMap::new,
                        Collectors.averagingDouble(Medicion::getTemperatura)
                ))
                .entrySet().stream()
                .map(e -> new Estadisticas.PuntoTemporal(e.getKey(), redondear(e.getValue())))
                .collect(Collectors.toList());
    }

    private Double redondear(Double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}