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
        this.repositoryPort      = repositoryPort;
        this.notificacionUseCase = notificacionUseCase;
    }

    @Override
    public AlertaDTO crear(AlertaDTO dto) {
        dto.setFecha(LocalDateTime.now());
        Alerta guardada = repositoryPort.guardar(toEntity(dto));

        String nivelNotificacion = mapearNivel(guardada.getNivel());
        notificacionUseCase.notificarAlerta(guardada.getMensaje(), nivelNotificacion);

        return toDTO(guardada);
    }

    @Override
    public AlertaDTO actualizar(Long id, AlertaDTO dto) {

        // ✅ cargar la alerta existente para preservar la fecha
        Alerta existente = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));

        // ✅ solo actualizar los campos que el usuario puede cambiar
        existente.setNivel(dto.getNivel());
        existente.setMensaje(dto.getMensaje());
        existente.setIdUsuario(dto.getIdUsuario());
        existente.setIdEvento(dto.getIdEvento());
        // fecha NO se toca — se preserva la original

        return toDTO(repositoryPort.guardar(existente));
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

    private String mapearNivel(NivelAlerta nivel) {
        if (nivel == null) return "BAJA";
        return switch (nivel) {
            case Roja     -> "ALTA";
            case Naranja  -> "ALTA";
            case Amarilla -> "MEDIA";
            case Verde    -> "BAJA";
        };
    }

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