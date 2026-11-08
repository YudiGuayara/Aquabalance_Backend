package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.events.domain.Evento;
import com.AquaBalance.events.domain.EventoRepository;
import com.AquaBalance.reports.application.ports.out.EventoConsultaPort;
import com.AquaBalance.reports.domain.Estadisticas;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EventoConsultaAdapter implements EventoConsultaPort {

    private final EventoRepository eventoRepository;

    public EventoConsultaAdapter(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    private List<Evento> filtrar(Long recursoId, Long contaminanteId,
                                 LocalDateTime inicio, LocalDateTime fin) {
        return eventoRepository.listarTodos().stream()
                .filter(e -> recursoId.equals(e.getIdRecurso()))
                .filter(e -> contaminanteId.equals(e.getIdContaminante()))
                .filter(e -> e.getFecha() != null)
                .filter(e -> !e.getFecha().isBefore(inicio) && !e.getFecha().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public long contarPorRecursoContaminanteYPeriodo(Long r, Long c,
                                                     LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).size();
    }

    @Override
    public Map<String, Long> eventosPorMagnitud(Long r, Long c,
                                                LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).stream()
                .collect(Collectors.groupingBy(Evento::getMagnitud, Collectors.counting()));
    }

    @Override
    public List<Estadisticas.EventoResumen> listarPorRecursoContaminanteYPeriodo(
            Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).stream()
                .map(e -> new Estadisticas.EventoResumen(
                        e.getId(), e.getDescripcion(), e.getMagnitud(), e.getFecha()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> idsEventosPorRecursoContaminanteYPeriodo(
            Long r, Long c, LocalDateTime i, LocalDateTime f) {
        return filtrar(r, c, i, f).stream()
                .map(Evento::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}