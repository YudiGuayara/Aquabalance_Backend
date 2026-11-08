package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.reports.application.ports.out.InformeRepositoryPort;
import com.AquaBalance.reports.domain.Informe;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InformeRepositoryImpl implements InformeRepositoryPort {

    private final JpaInformeRepository jpaRepository;

    public InformeRepositoryImpl(JpaInformeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Informe save(Informe informe) {
        return toDomain(jpaRepository.save(toEntity(informe)));
    }

    @Override
    public Optional<Informe> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Informe> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    private InformeEntity toEntity(Informe i) {
        InformeEntity e = new InformeEntity();
        e.setId(i.getId());
        e.setTitulo(i.getTitulo());
        e.setDescripcion(i.getDescripcion());
        e.setFechaGeneracion(i.getFechaGeneracion());
        e.setFechaInicio(i.getFechaInicio());
        e.setFechaFin(i.getFechaFin());
        e.setRecursoId(i.getRecursoId());
        e.setContaminanteId(i.getContaminanteId());
        return e;
    }

    private Informe toDomain(InformeEntity e) {
        Informe i = new Informe();
        i.setId(e.getId());
        i.setTitulo(e.getTitulo());
        i.setDescripcion(e.getDescripcion());
        i.setFechaGeneracion(e.getFechaGeneracion());
        i.setFechaInicio(e.getFechaInicio());
        i.setFechaFin(e.getFechaFin());
        i.setRecursoId(e.getRecursoId());
        i.setContaminanteId(e.getContaminanteId());
        return i;
    }
}