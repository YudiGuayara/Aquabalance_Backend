package com.AquaBalance.reports.application.ports.out;

import com.AquaBalance.reports.domain.Estadisticas;
import java.util.List;
import java.util.Map;

public interface AlertaConsultaPort {

    long contarPorEventos(List<Long> idsEventos);

    Map<String, Long> alertasPorNivel(List<Long> idsEventos);

    List<Estadisticas.AlertaResumen> listarPorEventos(List<Long> idsEventos);
}