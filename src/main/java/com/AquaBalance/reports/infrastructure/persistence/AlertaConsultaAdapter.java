package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.AlertaRepository;
import com.AquaBalance.reports.application.ports.out.AlertaConsultaPort;
import com.AquaBalance.reports.domain.Estadisticas;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlertaConsultaAdapter implements AlertaConsultaPort {

    private final AlertaRepository alertaRepository;

    public AlertaConsultaAdapter(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    private List<Alerta> filtrar(List<Long> idsEventos) {
        return alertaRepository.listarTodos().stream()
                .filter(a -> a.getIdEvento() != null && idsEventos.contains(a.getIdEvento()))
                .collect(Collectors.toList());
    }

    @Override
    public long contarPorEventos(List<Long> idsEventos) {
        return filtrar(idsEventos).size();
    }

    @Override
    public Map<String, Long> alertasPorNivel(List<Long> idsEventos) {
        return filtrar(idsEventos).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getNivel() == null ? "SIN NIVEL" : a.getNivel().name(),
                        Collectors.counting()
                ));
    }

    @Override
    public List<Estadisticas.AlertaResumen> listarPorEventos(List<Long> idsEventos) {
        return filtrar(idsEventos).stream()
                .map(a -> new Estadisticas.AlertaResumen(
                        a.getId(),
                        a.getNivel() == null ? "SIN NIVEL" : a.getNivel().name(),
                        a.getMensaje(),
                        a.getFecha()
                ))
                .collect(Collectors.toList());
    }
}