package com.AquaBalance.events.infrastructure.persistence;

import com.AquaBalance.events.application.ports.out.EventoRepositoryPort;
import com.AquaBalance.events.domain.Evento;
import com.AquaBalance.events.domain.EventoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventoRepositoryImpl implements EventoRepository, EventoRepositoryPort {

    private final JpaEventoRepository jpa;

    public EventoRepositoryImpl(JpaEventoRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Evento guardar(Evento evento) {
        return toDomain(jpa.save(toEntity(evento)));
    }

    @Override
    public Optional<Evento> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Evento> listarTodos() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Evento> buscarPorRecurso(Long idRecurso) {
        return jpa.findByIdRecurso(idRecurso).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private EventoEntity toEntity(Evento e) {
        return new EventoEntity(
                e.getId(), e.getDescripcion(), e.getMagnitud(),
                e.getFecha(), e.getIdContaminante(), e.getIdRecurso()
        );
    }

    private Evento toDomain(EventoEntity e) {
        return new Evento(
                e.getId(), e.getDescripcion(), e.getMagnitud(),
                e.getFecha(), e.getIdContaminante(), e.getIdRecurso()
        );
    }
}