package com.AquaBalance.monitoring.application;

import com.AquaBalance.monitoring.application.ports.in.RegistrarMedicionUseCase;
import com.AquaBalance.monitoring.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoring.application.ports.out.MedicionRepositoryPort;
import com.AquaBalance.monitoring.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.Medicion;
import com.AquaBalance.monitoring.domain.Recurso;
import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicionService implements RegistrarMedicionUseCase {

    // ── Umbrales de alerta ────────────────────────────────────────────────────
    private static final double PH_CRITICO_MIN     = 5.0;
    private static final double PH_CRITICO_MAX     = 9.0;
    private static final double PH_ADVERTENCIA_MIN = 6.0;
    private static final double PH_ADVERTENCIA_MAX = 8.5;
    private static final double TEMP_CRITICA_MAX   = 35.0;
    private static final double TEMP_ADVERTENCIA_MAX = 30.0;

    private final MedicionRepositoryPort       repositoryPort;
    private final RecursoRepositoryPort        recursoRepository;
    private final ContaminanteRepositoryPort   contaminanteRepository;
    private final GestionarNotificacionUseCase notificacionUseCase;

    public MedicionService(MedicionRepositoryPort repositoryPort,
                           RecursoRepositoryPort recursoRepository,
                           ContaminanteRepositoryPort contaminanteRepository,
                           GestionarNotificacionUseCase notificacionUseCase) {
        this.repositoryPort       = repositoryPort;
        this.recursoRepository    = recursoRepository;
        this.contaminanteRepository = contaminanteRepository;
        this.notificacionUseCase  = notificacionUseCase;
    }

    // ── Registrar ─────────────────────────────────────────────────────────────

    @Override
    public MedicionDTO registrar(MedicionDTO dto) {
        validarMedicion(dto);
        dto.setFecha(LocalDateTime.now());

        Medicion guardada = repositoryPort.guardar(toEntity(dto));
        MedicionDTO resultado = enrichDTO(toDTO(guardada));

        // Notificación de medición registrada (siempre, nivel BAJA)
        notificacionUseCase.notificarMedicion(
                resultado.getNombreRecurso(),
                resultado.getNombreContaminante(),
                resultado.getPh()
        );

        // Alertas por pH fuera de rango
        evaluarAlertaPh(resultado);

        // Alertas por temperatura fuera de rango
        evaluarAlertaTemperatura(resultado);

        return resultado;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MedicionDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(m -> enrichDTO(toDTO(m)))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Medicion no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicionDTO> listarTodos() {
        return repositoryPort.listarTodos()
                .stream()
                .map(m -> enrichDTO(toDTO(m)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicionDTO> listarPorRecurso(Long idRecurso) {
        return repositoryPort.buscarPorRecurso(idRecurso)
                .stream()
                .map(m -> enrichDTO(toDTO(m)))
                .collect(Collectors.toList());
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    @Override
    public MedicionDTO actualizar(Long id, MedicionDTO dto) {
        Medicion existente = repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Medicion no encontrada con id: " + id));

        existente.setPh(dto.getPh());
        existente.setTemperatura(dto.getTemperatura());
        existente.setIdRecurso(dto.getIdRecurso());
        existente.setIdContaminante(dto.getIdContaminante());
        existente.setIdUsuario(dto.getIdUsuario());

        MedicionDTO resultado = enrichDTO(toDTO(repositoryPort.guardar(existente)));

        // Re-evaluar alertas al actualizar
        evaluarAlertaPh(resultado);
        evaluarAlertaTemperatura(resultado);

        return resultado;
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Override
    public void eliminar(Long id) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Medicion no encontrada con id: " + id));
        repositoryPort.eliminar(id);
    }

    // ── Lógica de alertas ─────────────────────────────────────────────────────

    private void evaluarAlertaPh(MedicionDTO dto) {
        if (dto.getPh() == null) return;

        double ph = dto.getPh();
        String recurso = dto.getNombreRecurso();
        String cont    = dto.getNombreContaminante();

        if (ph < PH_CRITICO_MIN || ph > PH_CRITICO_MAX) {
            notificacionUseCase.notificarAlerta(
                    String.format("pH CRÍTICO en %s — Contaminante: %s | pH: %.2f " +
                                    "(rango seguro: %.1f–%.1f)",
                            recurso, cont, ph, PH_CRITICO_MIN, PH_CRITICO_MAX),
                    "ALTA"
            );
        } else if (ph < PH_ADVERTENCIA_MIN || ph > PH_ADVERTENCIA_MAX) {
            notificacionUseCase.notificarAlerta(
                    String.format("pH fuera del rango óptimo en %s — Contaminante: %s | pH: %.2f " +
                                    "(óptimo: %.1f–%.1f)",
                            recurso, cont, ph, PH_ADVERTENCIA_MIN, PH_ADVERTENCIA_MAX),
                    "MEDIA"
            );
        }
    }

    private void evaluarAlertaTemperatura(MedicionDTO dto) {
        if (dto.getTemperatura() == null) return;

        double temp  = dto.getTemperatura();
        String recurso = dto.getNombreRecurso();

        if (temp > TEMP_CRITICA_MAX) {
            notificacionUseCase.notificarAlerta(
                    String.format("Temperatura CRÍTICA en %s | %.1f °C (máximo seguro: %.1f °C)",
                            recurso, temp, TEMP_CRITICA_MAX),
                    "ALTA"
            );
        } else if (temp > TEMP_ADVERTENCIA_MAX) {
            notificacionUseCase.notificarAlerta(
                    String.format("Temperatura elevada en %s | %.1f °C (óptimo: ≤ %.1f °C)",
                            recurso, temp, TEMP_ADVERTENCIA_MAX),
                    "MEDIA"
            );
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MedicionDTO enrichDTO(MedicionDTO dto) {
        String nombreRecurso = recursoRepository.buscarPorId(dto.getIdRecurso())
                .map(Recurso::getNombre)
                .orElse("Desconocido");

        String nombreContaminante = contaminanteRepository.buscarPorId(dto.getIdContaminante())
                .map(Contaminante::getNombre)
                .orElse("Desconocido");

        dto.setNombreRecurso(nombreRecurso);
        dto.setNombreContaminante(nombreContaminante);
        return dto;
    }

    private void validarMedicion(MedicionDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("La medición no puede ser nula");
        if (dto.getPh() == null)
            throw new IllegalArgumentException("El pH es obligatorio");
        if (dto.getPh() < 0 || dto.getPh() > 14)
            throw new IllegalArgumentException("El pH debe estar entre 0 y 14");
        if (dto.getTemperatura() == null)
            throw new IllegalArgumentException("La temperatura es obligatoria");
        if (dto.getTemperatura() < -50 || dto.getTemperatura() > 100)
            throw new IllegalArgumentException("Temperatura fuera de rango realista");
        if (dto.getIdRecurso() == null || dto.getIdRecurso() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un recurso válido");
        if (dto.getIdContaminante() == null || dto.getIdContaminante() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un contaminante válido");
        if (dto.getIdUsuario() == null || dto.getIdUsuario() <= 0)
            throw new IllegalArgumentException("Usuario inválido");
    }

    private Medicion toEntity(MedicionDTO dto) {
        return new Medicion(
                dto.getId(), dto.getPh(), dto.getTemperatura(), dto.getFecha(),
                dto.getIdUsuario(), dto.getIdRecurso(), dto.getIdContaminante()
        );
    }

    private MedicionDTO toDTO(Medicion m) {
        return new MedicionDTO(
                m.getId(), m.getPh(), m.getTemperatura(), m.getFecha(),
                m.getIdUsuario(), m.getIdRecurso(), m.getIdContaminante()
        );
    }
}