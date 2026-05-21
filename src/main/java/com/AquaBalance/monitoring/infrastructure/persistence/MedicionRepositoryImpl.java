package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.application.ports.out.MedicionRepositoryPort;
import com.AquaBalance.monitoring.domain.Medicion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MedicionRepositoryImpl implements MedicionRepositoryPort {

    private final JpaMedicionRepository jpa;

    public MedicionRepositoryImpl(JpaMedicionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Medicion guardar(Medicion medicion) {
        return toDomain(jpa.save(toEntity(medicion)));
    }

    @Override
    public Optional<Medicion> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Medicion> listarTodos() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Medicion> buscarPorRecurso(Long idRecurso) {
        return jpa.findByIdRecurso(idRecurso).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private MedicionEntity toEntity(Medicion m) {
        return new MedicionEntity(
                m.getId(), m.getPh(), m.getTemperatura(), m.getFecha(),
                m.getIdUsuario(), m.getIdRecurso(), m.getIdContaminante()
        );
    }

    private Medicion toDomain(MedicionEntity e) {
        return new Medicion(
                e.getId(), e.getPh(), e.getTemperatura(), e.getFecha(),
                e.getIdUsuario(), e.getIdRecurso(), e.getIdContaminante()
        );
    }
}