package com.AquaBalance.events.infrastructure.persistence;

import com.AquaBalance.events.application.ports.out.AlertaRepositoryPort;
import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.AlertaRepository;
import com.AquaBalance.events.domain.NivelAlerta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AlertaRepositoryImpl implements AlertaRepository, AlertaRepositoryPort {

    private final JpaAlertaRepository jpa;

    public AlertaRepositoryImpl(JpaAlertaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Alerta guardar(Alerta alerta) {
        return toDomain(jpa.save(toEntity(alerta)));
    }

    @Override
    public Optional<Alerta> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Alerta> listarTodos() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Alerta> buscarPorNivel(NivelAlerta nivel) {
        return jpa.findByNivel(nivel).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Alerta> buscarPorEvento(Long idEvento) {
        return jpa.findByIdEvento(idEvento).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    private AlertaEntity toEntity(Alerta a) {
        return new AlertaEntity(
                a.getId(), a.getFecha(), a.getNivel(),
                a.getMensaje(), a.getIdUsuario(), a.getIdEvento()
        );
    }

    private Alerta toDomain(AlertaEntity e) {
        return new Alerta(
                e.getId(), e.getFecha(), e.getNivel(),
                e.getMensaje(), e.getIdUsuario(), e.getIdEvento()
        );
    }
}