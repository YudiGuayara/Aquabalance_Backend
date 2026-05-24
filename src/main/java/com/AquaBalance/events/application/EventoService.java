package com.AquaBalance.events.application;


import com.AquaBalance.events.application.ports.in.GestionarEventoUseCase;
import com.AquaBalance.events.application.ports.out.EventoRepositoryPort;
import com.AquaBalance.events.domain.Evento;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService implements GestionarEventoUseCase {

    private final EventoRepositoryPort repositoryPort;

    public EventoService(EventoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public EventoDTO crear(EventoDTO dto) {
        dto.setFecha(LocalDateTime.now());
        return toDTO(repositoryPort.guardar(toEntity(dto)));
    }

    @Override
    public EventoDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    @Override
    public List<EventoDTO> listarTodos() {
        return repositoryPort.listarTodos()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> listarPorRecurso(Long idRecurso) {
        return repositoryPort.buscarPorRecurso(idRecurso)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventoDTO actualizar(Long id, EventoDTO dto) {

        Evento evento = repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado con id: " + id));

        evento.setDescripcion(dto.getDescripcion());
        evento.setMagnitud(dto.getMagnitud());
        evento.setIdContaminante(dto.getIdContaminante());
        evento.setIdRecurso(dto.getIdRecurso());

        return toDTO(repositoryPort.guardar(evento));
    }

    @Override
    public void eliminar(Long id) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        repositoryPort.eliminar(id);
    }

    private Evento toEntity(EventoDTO dto) {
        return new Evento(dto.getId(), dto.getDescripcion(), dto.getMagnitud(), dto.getFecha(), dto.getIdContaminante(), dto.getIdRecurso());
    }

    private EventoDTO toDTO(Evento e) {
        return new EventoDTO(e.getId(), e.getDescripcion(), e.getMagnitud(), e.getFecha(), e.getIdContaminante(), e.getIdRecurso());
    }
}