package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoring.domain.Recurso;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RecursoRepositoryImpl implements RecursoRepositoryPort {

    private final JpaRecursoRepository jpa;

    public RecursoRepositoryImpl(JpaRecursoRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Recurso guardar(Recurso recurso) {
        RecursoEntity entity = toEntity(recurso);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Recurso> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Recurso> listarTodos() {
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private RecursoEntity toEntity(Recurso r) {
        return new RecursoEntity(r.getId(), r.getNombre(), r.getTipo(), r.getUbicacion(), r.getLatitud(), r.getLongitud());
    }

    private Recurso toDomain(RecursoEntity e) {
        return new Recurso(e.getId(), e.getNombre(), e.getTipo(), e.getUbicacion(), e.getLatitud(), e.getLongitud());
    }
}