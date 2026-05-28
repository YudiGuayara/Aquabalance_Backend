package com.AquaBalance.reports.application;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
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

    private final InformeRepositoryPort       informePort;
    private final RecursoConsultaPort         recursoPort;
    private final ContaminanteConsultaPort    contaminantePort;
    private final MedicionConsultaPort        medicionPort;
    private final EventoConsultaPort          eventoPort;
    private final AlertaConsultaPort          alertaPort;
    private final GestionarNotificacionUseCase notificacionUseCase;

    public InformeService(
            InformeRepositoryPort informePort,
            RecursoConsultaPort recursoPort,
            ContaminanteConsultaPort contaminantePort,
            MedicionConsultaPort medicionPort,
            EventoConsultaPort eventoPort,
            AlertaConsultaPort alertaPort,
            GestionarNotificacionUseCase notificacionUseCase) {
        this.informePort         = informePort;
        this.recursoPort         = recursoPort;
        this.contaminantePort    = contaminantePort;
        this.medicionPort        = medicionPort;
        this.eventoPort          = eventoPort;
        this.alertaPort          = alertaPort;
        this.notificacionUseCase = notificacionUseCase;
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Informe crearInforme(Informe informe) {
        validarFechas(informe.getFechaInicio(), informe.getFechaFin());
        validarExistencia(informe.getRecursoId(), informe.getContaminanteId());
        informe.setFechaGeneracion(LocalDateTime.now());

        Informe guardado = informePort.save(informe);

        // Notificar generación del informe
        notificacionUseCase.notificarInforme(guardado.getTitulo());

        return guardado;
    }

    // ── Obtener con estadísticas ──────────────────────────────────────────────

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

        // Alertar si las estadísticas muestran valores críticos
        evaluarEstadisticasCriticas(informe.getTitulo(), nombreRecurso, stats);

        return new InformeDetalle(informe, nombreRecurso, nombreContaminante, stats);
    }

    // ── Listar ────────────────────────────────────────────────────────────────

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

    // ── Actualizar ────────────────────────────────────────────────────────────

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

        Informe actualizado = informePort.save(existente);

        // Notificar actualización del informe
        notificacionUseCase.notificarInforme("(Actualizado) " + actualizado.getTitulo());

        return actualizado;
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminarInforme(Long id) {
        if (!informePort.existsById(id))
            throw new ResourceNotFoundException("Informe no encontrado: " + id);
        informePort.deleteById(id);
    }

    // ── Evaluación de estadísticas críticas ───────────────────────────────────

    /**
     * Si las estadísticas calculadas revelan valores fuera de rango se dispara
     * una alerta adicional de nivel ALTA o MEDIA para que el equipo lo revise.
     */
    private void evaluarEstadisticasCriticas(String tituloInforme,
                                             String nombreRecurso,
                                             Estadisticas stats) {
        // pH mínimo crítico
        if (stats.getPhMinimo() != null && stats.getPhMinimo() < 5.0) {
            notificacionUseCase.notificarAlerta(
                    String.format("Informe '%s' — pH mínimo crítico detectado en %s: %.2f",
                            tituloInforme, nombreRecurso, stats.getPhMinimo()),
                    "ALTA"
            );
        }
        // pH máximo crítico
        if (stats.getPhMaximo() != null && stats.getPhMaximo() > 9.0) {
            notificacionUseCase.notificarAlerta(
                    String.format("Informe '%s' — pH máximo crítico detectado en %s: %.2f",
                            tituloInforme, nombreRecurso, stats.getPhMaximo()),
                    "ALTA"
            );
        }
        // Temperatura máxima crítica
        if (stats.getTemperaturaMaxima() != null && stats.getTemperaturaMaxima() > 35.0) {
            notificacionUseCase.notificarAlerta(
                    String.format("Informe '%s' — Temperatura máxima crítica en %s: %.1f °C",
                            tituloInforme, nombreRecurso, stats.getTemperaturaMaxima()),
                    "ALTA"
            );
        }
        // Muchas alertas en el periodo
        if (stats.getTotalAlertas() != null && stats.getTotalAlertas() >= 5) {
            notificacionUseCase.notificarAlerta(
                    String.format("Informe '%s' — %d alertas registradas en el período analizado en %s",
                            tituloInforme, stats.getTotalAlertas(), nombreRecurso),
                    "MEDIA"
            );
        }
    }

    // ── Cálculo de estadísticas ───────────────────────────────────────────────

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

    // ── Validaciones ──────────────────────────────────────────────────────────

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