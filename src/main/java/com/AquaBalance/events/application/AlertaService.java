package com.AquaBalance.events.application;

import com.AquaBalance.events.application.ports.in.GestionarAlertaUseCase;
import com.AquaBalance.events.application.ports.out.AlertaRepositoryPort;
import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.NivelAlerta;
import com.AquaBalance.notifications.application.NotificacionService;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertaService implements GestionarAlertaUseCase {

    private final AlertaRepositoryPort repositoryPort;
    private final NotificacionService  notificacionService;

    public AlertaService(AlertaRepositoryPort repositoryPort,
                         NotificacionService notificacionService) {
        this.repositoryPort      = repositoryPort;
        this.notificacionService = notificacionService;
    }

    @Override
    public AlertaDTO crear(AlertaDTO dto) {
        dto.setFecha(LocalDateTime.now());
        AlertaDTO creada = toDTO(repositoryPort.guardar(toEntity(dto)));

        // ✅ Notificar al crear alerta
        notificacionService.notificarAlerta(
                creada.getMensaje(),
                creada.getNivel() != null ? creada.getNivel().name() : "BAJA"
        );

        return creada;
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
    public AlertaDTO actualizar(Long id, AlertaDTO dto) {
        Alerta alerta = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));

        alerta.setNivel(dto.getNivel());
        alerta.setMensaje(dto.getMensaje());
        alerta.setIdUsuario(dto.getIdUsuario());
        alerta.setIdEvento(dto.getIdEvento());

        return toDTO(repositoryPort.guardar(alerta));
    }

    @Override
    public void eliminar(Long id) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));
        repositoryPort.eliminar(id);
    }

    private Alerta toEntity(AlertaDTO dto) {
        return new Alerta(
                dto.getId(), dto.getFecha(), dto.getNivel(),
                dto.getMensaje(), dto.getIdUsuario(), dto.getIdEvento()
        );
    }

    private AlertaDTO toDTO(Alerta a) {
        return new AlertaDTO(
                a.getId(), a.getFecha(), a.getNivel(),
                a.getMensaje(), a.getIdUsuario(), a.getIdEvento()
        );
    }
}