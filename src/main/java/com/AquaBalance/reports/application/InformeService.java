package com.AquaBalance.reports.application;

import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase;
import com.AquaBalance.reports.application.ports.out.*;
import com.AquaBalance.reports.domain.Estadisticas;
import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InformeService implements GestionarInformeUseCase {

    private final InformeRepositoryPort    informePort;
    private final RecursoConsultaPort      recursoPort;
    private final ContaminanteConsultaPort contaminantePort;
    private final MedicionConsultaPort     medicionPort;
    private final EventoConsultaPort       eventoPort;
    private final AlertaConsultaPort       alertaPort;

    public InformeService(
            InformeRepositoryPort informePort,
            RecursoConsultaPort recursoPort,
            ContaminanteConsultaPort contaminantePort,
            MedicionConsultaPort medicionPort,
            EventoConsultaPort eventoPort,
            AlertaConsultaPort alertaPort) {
        this.informePort      = informePort;
        this.recursoPort      = recursoPort;
        this.contaminantePort = contaminantePort;
        this.medicionPort     = medicionPort;
        this.eventoPort       = eventoPort;
        this.alertaPort       = alertaPort;
    }

    // ─── CREAR ───────────────────────────────────────────────

    @Override
    @Transactional
    public Informe crearInforme(Informe informe) {
        validarFechas(informe.getFechaInicio(), informe.getFechaFin());
        validarExistencia(informe.getRecursoId(), informe.getContaminanteId());
        informe.setFechaGeneracion(LocalDateTime.now());
        return informePort.save(informe);
    }

    // ─── OBTENER CON ESTADÍSTICAS ────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InformeDetalle obtenerInforme(Long id) {
        Informe informe = informePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Informe no encontrado: " + id));

        String nombreRecurso = recursoPort.buscarNombrePorId(informe.getRecursoId())
                .orElse("Desconocido");
        String nombreContaminante = contaminantePort.buscarNombrePorId(informe.getContaminanteId())
                .orElse("Desconocido");

        Estadisticas stats = calcularEstadisticas(
                informe.getRecursoId(), informe.getContaminanteId(),
                informe.getFechaInicio(), informe.getFechaFin()
        );

        return new InformeDetalle(informe, nombreRecurso, nombreContaminante, stats);
    }

    // ─── LISTAR ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InformeResumen> listarInformes() {
        return informePort.findAll().stream()
                .map(i -> new InformeResumen(
                        i,
                        recursoPort.buscarNombrePorId(i.getRecursoId()).orElse("Desconocido"),
                        contaminantePort.buscarNombrePorId(i.getContaminanteId()).orElse("Desconocido")
                ))
                .collect(Collectors.toList());
    }

    // ─── ACTUALIZAR ──────────────────────────────────────────

    @Override
    @Transactional
    public Informe actualizarInforme(Long id, Informe nuevo) {
        Informe existente = informePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Informe no encontrado: " + id));

        validarFechas(nuevo.getFechaInicio(), nuevo.getFechaFin());
        validarExistencia(nuevo.getRecursoId(), nuevo.getContaminanteId());

        existente.setTitulo(nuevo.getTitulo());
        existente.setDescripcion(nuevo.getDescripcion());
        existente.setFechaInicio(nuevo.getFechaInicio());
        existente.setFechaFin(nuevo.getFechaFin());
        existente.setRecursoId(nuevo.getRecursoId());
        existente.setContaminanteId(nuevo.getContaminanteId());

        return informePort.save(existente);
    }

    // ─── ELIMINAR ────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminarInforme(Long id) {
        if (!informePort.existsById(id))
            throw new ResourceNotFoundException("Informe no encontrado: " + id);
        informePort.deleteById(id);
    }

    // ─── CÁLCULO DE ESTADÍSTICAS (lógica de coordinación) ───

    private Estadisticas calcularEstadisticas(Long recursoId, Long contaminanteId,
                                              LocalDateTime inicio, LocalDateTime fin) {
        Estadisticas stats = new Estadisticas();
        stats.setFechaInicio(inicio);
        stats.setFechaFin(fin);

        // Mediciones
        stats.setTotalMediciones(
                medicionPort.contarPorRecursoContaminanteYPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setPromedioPh(
                medicionPort.promedioPhPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setPromedioTemperatura(
                medicionPort.promedioTemperaturaPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setPhMinimo(
                medicionPort.minimoPhPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setPhMaximo(
                medicionPort.maximoPhPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setTemperaturaMinima(
                medicionPort.minimoTemperaturaPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setTemperaturaMaxima(
                medicionPort.maximoTemperaturaPorPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setMedicionesPorContaminante(
                medicionPort.medicionesPorContaminante(recursoId, contaminanteId, inicio, fin));
        stats.setEvolucionPh(
                medicionPort.evolucionPh(recursoId, contaminanteId, inicio, fin));
        stats.setEvolucionTemperatura(
                medicionPort.evolucionTemperatura(recursoId, contaminanteId, inicio, fin));

        // Eventos
        stats.setTotalEventos(
                eventoPort.contarPorRecursoContaminanteYPeriodo(recursoId, contaminanteId, inicio, fin));
        stats.setEventosPorMagnitud(
                eventoPort.eventosPorMagnitud(recursoId, contaminanteId, inicio, fin));
        stats.setEventos(
                eventoPort.listarPorRecursoContaminanteYPeriodo(recursoId, contaminanteId, inicio, fin));

        // Alertas
        List<Long> idsEventos = eventoPort
                .idsEventosPorRecursoContaminanteYPeriodo(recursoId, contaminanteId, inicio, fin);

        if (idsEventos.isEmpty()) {
            stats.setTotalAlertas(0L);
            stats.setAlertasPorNivel(Collections.emptyMap());
            stats.setAlertas(Collections.emptyList());
        } else {
            stats.setTotalAlertas(alertaPort.contarPorEventos(idsEventos));
            stats.setAlertasPorNivel(alertaPort.alertasPorNivel(idsEventos));
            stats.setAlertas(alertaPort.listarPorEventos(idsEventos));
        }

        return stats;
    }

    // ─── VALIDACIONES ────────────────────────────────────────

    private void validarFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null)
            throw new IllegalArgumentException("Las fechas son obligatorias");
        if (fin.isBefore(inicio))
            throw new IllegalArgumentException("La fecha fin no puede ser menor a la fecha inicio");
    }

    private void validarExistencia(Long recursoId, Long contaminanteId) {
        recursoPort.buscarNombrePorId(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + recursoId));
        contaminantePort.buscarNombrePorId(contaminanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Contaminante no encontrado: " + contaminanteId));
    }
}