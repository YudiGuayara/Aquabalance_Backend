package com.AquaBalance.events.application;

import com.AquaBalance.events.application.ports.in.GestionarAlertaUseCase;
import com.AquaBalance.events.application.ports.out.AlertaRepositoryPort;
import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.NivelAlerta;
import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertaService implements GestionarAlertaUseCase {

    private final AlertaRepositoryPort repositoryPort;
    private final GestionarNotificacionUseCase notificacionUseCase;

    public AlertaService(AlertaRepositoryPort repositoryPort,
                         GestionarNotificacionUseCase notificacionUseCase) {
        this.repositoryPort   = repositoryPort;
        this.notificacionUseCase = notificacionUseCase;
    }

    @Override
    public AlertaDTO crear(AlertaDTO dto) {
        dto.setFecha(LocalDateTime.now());
        Alerta guardada = repositoryPort.guardar(toEntity(dto));

        // Mapear NivelAlerta → nivel de notificación (ALTA / MEDIA / BAJA)
        String nivelNotificacion = mapearNivel(guardada.getNivel());
        notificacionUseCase.notificarAlerta(guardada.getMensaje(), nivelNotificacion);

        return toDTO(guardada);
    }

    @Override
    public AlertaDTO actualizar(Long id, AlertaDTO dto) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));
        Alerta alerta = toEntity(dto);
        alerta.setId(id);
        return toDTO(repositoryPort.guardar(alerta));
    }

    @Override
    public AlertaDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));
    }

    @Override
    public List<AlertaDTO> listarTodos() {
        return repositoryPort.listarTodos()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertaDTO> listarPorNivel(NivelAlerta nivel) {
        return repositoryPort.buscarPorNivel(nivel)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertaDTO> listarPorEvento(Long idEvento) {
        return repositoryPort.buscarPorEvento(idEvento)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));
        repositoryPort.eliminar(id);
    }

    // ── Mapeo NivelAlerta → String para notificaciones ────────────────────────

    /**
     * Convierte el enum NivelAlerta al nivel string que usa el sistema
     * de notificaciones para decidir si envía email (ALTA / MEDIA) o no (BAJA).
     *
     *  Roja     → ALTA   → guarda en BD + WebSocket + Email
     *  Naranja  → ALTA   → guarda en BD + WebSocket + Email
     *  Amarilla → MEDIA  → guarda en BD + WebSocket + Email
     *  Verde    → BAJA   → guarda en BD + WebSocket  (sin email)
     */
    private String mapearNivel(NivelAlerta nivel) {
        if (nivel == null) return "BAJA";
        return switch (nivel) {
            case Roja     -> "ALTA";
            case Naranja  -> "ALTA";
            case Amarilla -> "MEDIA";
            case Verde    -> "BAJA";
        };
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private Alerta toEntity(AlertaDTO dto) {
        return new Alerta(
                dto.getId(), dto.getFecha(), dto.getNivel(),
                dto.getMensaje(), dto.getIdUsuario(), dto.getIdEvento());
    }

    private AlertaDTO toDTO(Alerta a) {
        return new AlertaDTO(
                a.getId(), a.getFecha(), a.getNivel(),
                a.getMensaje(), a.getIdUsuario(), a.getIdEvento());
    }
}