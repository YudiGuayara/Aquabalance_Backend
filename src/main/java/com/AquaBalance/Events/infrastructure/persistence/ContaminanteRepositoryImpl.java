package com.AquaBalance.monitoreo.infrastructure.persistence;

import com.AquaBalance.monitoreo.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoreo.domain.Contaminante;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContaminanteRepositoryImpl implements ContaminanteRepositoryPort {

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
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private ContaminanteEntity toEntity(Contaminante c) {
        return new ContaminanteEntity(c.getId(), c.getNombre(), c.getCarga(), c.getNivel(), c.getFuenteOrigen());
    }

    private Contaminante toDomain(ContaminanteEntity e) {
        return new Contaminante(e.getId(), e.getNombre(), e.getCarga(), e.getNivel(), e.getFuenteOrigen());
    }
}