package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.ContaminanteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContaminanteRepositoryImpl implements ContaminanteRepository, ContaminanteRepositoryPort {

    private final JpaContaminanteRepository jpa;

    public ContaminanteRepositoryImpl(JpaContaminanteRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Contaminante guardar(Contaminante contaminante) {
        return toDomain(jpa.save(toEntity(contaminante)));
    }

    @Override
    public Optional<Contaminante> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Contaminante> listarTodos() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private ContaminanteEntity toEntity(Contaminante c) {
        ContaminanteEntity e = new ContaminanteEntity();
        e.setId(c.getId());
        e.setNombre(c.getNombre());
        e.setCarga(c.getCarga());
        e.setNivel(c.getNivel());
        e.setFuenteOrigen(c.getFuenteOrigen());
        return e;
    }

    private Contaminante toDomain(ContaminanteEntity e) {
        Contaminante c = new Contaminante();
        c.setId(e.getId());
        c.setNombre(e.getNombre());
        c.setCarga(e.getCarga());
        c.setNivel(e.getNivel());
        c.setFuenteOrigen(e.getFuenteOrigen());
        return c;
    }
}