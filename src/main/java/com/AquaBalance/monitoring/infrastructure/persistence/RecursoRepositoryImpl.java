package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoring.domain.Recurso;
import com.AquaBalance.monitoring.domain.RecursoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RecursoRepositoryImpl implements RecursoRepository, RecursoRepositoryPort {

    private final JpaRecursoRepository jpa;

    public RecursoRepositoryImpl(JpaRecursoRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Recurso guardar(Recurso recurso) {
        return toDomain(jpa.save(toEntity(recurso)));
    }

    @Override
    public Optional<Recurso> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Recurso> listarTodos() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private RecursoEntity toEntity(Recurso r) {
        RecursoEntity e = new RecursoEntity();
        e.setId(r.getId());
        e.setNombre(r.getNombre());
        e.setTipo(r.getTipo());
        e.setUbicacion(r.getUbicacion());
        e.setLatitud(r.getLatitud());
        e.setLongitud(r.getLongitud());
        return e;
    }

    private Recurso toDomain(RecursoEntity e) {
        Recurso r = new Recurso();
        r.setId(e.getId());
        r.setNombre(e.getNombre());
        r.setTipo(e.getTipo());
        r.setUbicacion(e.getUbicacion());
        r.setLatitud(e.getLatitud());
        r.setLongitud(e.getLongitud());
        return r;
    }
}