package com.AquaBalance.reports.infrastructure.web;

import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase.*;
import com.AquaBalance.reports.domain.Estadisticas;
import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.reports.infrastructure.web.dto.*;

import java.util.stream.Collectors;

public class InformeMapper {

    private InformeMapper() {}

    public static Informe toDomain(InformeRequest req) {
        Informe i = new Informe();
        i.setTitulo(req.getTitulo());
        i.setDescripcion(req.getDescripcion());
        i.setFechaInicio(req.getFechaInicio());
        i.setFechaFin(req.getFechaFin());
        i.setRecursoId(req.getRecursoId());
        i.setContaminanteId(req.getContaminanteId());
        return i;
    }

    public static InformeResponse fromResumen(InformeResumen r) {
        InformeResponse res = new InformeResponse();
        res.setId(r.getInforme().getId());
        res.setTitulo(r.getInforme().getTitulo());
        res.setDescripcion(r.getInforme().getDescripcion());
        res.setFechaGeneracion(r.getInforme().getFechaGeneracion());
        res.setFechaInicio(r.getInforme().getFechaInicio());
        res.setFechaFin(r.getInforme().getFechaFin());
        res.setRecursoId(r.getInforme().getRecursoId());
        res.setNombreRecurso(r.getNombreRecurso());
        res.setContaminanteId(r.getInforme().getContaminanteId());
        res.setNombreContaminante(r.getNombreContaminante());
        return res;
    }

    public static InformeResponse fromDetalle(InformeDetalle d) {
        InformeResponse res = fromResumen(
                new InformeResumen(d.getInforme(), d.getNombreRecurso(), d.getNombreContaminante())
        );
        res.setEstadisticas(toEstadisticasResponse(d.getEstadisticas()));
        return res;
    }

    public static InformeResponse fromInforme(Informe i) {
        InformeResponse res = new InformeResponse();
        res.setId(i.getId());
        res.setTitulo(i.getTitulo());
        res.setDescripcion(i.getDescripcion());
        res.setFechaGeneracion(i.getFechaGeneracion());
        res.setFechaInicio(i.getFechaInicio());
        res.setFechaFin(i.getFechaFin());
        res.setRecursoId(i.getRecursoId());
        res.setContaminanteId(i.getContaminanteId());
        return res;
    }

    public static EstadisticasResponse toEstadisticasResponse(Estadisticas e) {
        if (e == null) return null;
        EstadisticasResponse res = new EstadisticasResponse();
        res.setTotalMediciones(e.getTotalMediciones());
        res.setPromedioPh(e.getPromedioPh());
        res.setPromedioTemperatura(e.getPromedioTemperatura());
        res.setPhMinimo(e.getPhMinimo());
        res.setPhMaximo(e.getPhMaximo());
        res.setTemperaturaMinima(e.getTemperaturaMinima());
        res.setTemperaturaMaxima(e.getTemperaturaMaxima());
        res.setMedicionesPorContaminante(e.getMedicionesPorContaminante());
        res.setEvolucionPh(e.getEvolucionPh() == null ? null :
                e.getEvolucionPh().stream()
                        .map(p -> new EstadisticasResponse.PuntoTemporalResponse(p.getFecha(), p.getValor()))
                        .collect(Collectors.toList()));
        res.setEvolucionTemperatura(e.getEvolucionTemperatura() == null ? null :
                e.getEvolucionTemperatura().stream()
                        .map(p -> new EstadisticasResponse.PuntoTemporalResponse(p.getFecha(), p.getValor()))
                        .collect(Collectors.toList()));
        res.setTotalAlertas(e.getTotalAlertas());
        res.setAlertasPorNivel(e.getAlertasPorNivel());
        res.setAlertas(e.getAlertas() == null ? null :
                e.getAlertas().stream()
                        .map(a -> new EstadisticasResponse.AlertaResumenResponse(
                                a.getId(), a.getNivel(), a.getMensaje(), a.getFecha()))
                        .collect(Collectors.toList()));
        res.setTotalEventos(e.getTotalEventos());
        res.setEventosPorMagnitud(e.getEventosPorMagnitud());
        res.setEventos(e.getEventos() == null ? null :
                e.getEventos().stream()
                        .map(ev -> new EstadisticasResponse.EventoResumenResponse(
                                ev.getId(), ev.getDescripcion(), ev.getMagnitud(), ev.getFecha()))
                        .collect(Collectors.toList()));
        res.setFechaInicio(e.getFechaInicio());
        res.setFechaFin(e.getFechaFin());
        return res;
    }
}