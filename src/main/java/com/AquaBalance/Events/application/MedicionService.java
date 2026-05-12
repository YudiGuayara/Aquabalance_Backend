package com.AquaBalance.monitoreo.application;

import com.AquaBalance.monitoreo.application.ports.in.RegistrarMedicionUseCase;
import com.AquaBalance.monitoreo.application.ports.out.MedicionRepositoryPort;
import com.AquaBalance.monitoreo.domain.Medicion;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicionService implements RegistrarMedicionUseCase {

    private final MedicionRepositoryPort repositoryPort;

    public MedicionService(MedicionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    // =========================
    // REGISTRAR
    // =========================
    @Override
    public MedicionDTO registrar(MedicionDTO dto) {

        validarMedicion(dto);

        dto.setFecha(LocalDateTime.now());

        return toDTO(repositoryPort.guardar(toEntity(dto)));
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    @Override
    public MedicionDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Medicion no encontrada con id: " + id));
    }

    // =========================
    // LISTAR TODOS
    // =========================
    @Override
    public List<MedicionDTO> listarTodos() {
        return repositoryPort.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // LISTAR POR RECURSO
    // =========================
    @Override
    public List<MedicionDTO> listarPorRecurso(Long idRecurso) {
        return repositoryPort.buscarPorRecurso(idRecurso)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // ELIMINAR
    // =========================
    @Override
    public void eliminar(Long id) {
        repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Medicion no encontrada con id: " + id));

        repositoryPort.eliminar(id);
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Override
    public MedicionDTO actualizar(Long id, MedicionDTO dto) {

        Medicion existente = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicion no encontrada con id: " + id));

        existente.setPh(dto.getPh());
        existente.setTemperatura(dto.getTemperatura());
        existente.setIdRecurso(dto.getIdRecurso());
        existente.setIdContaminante(dto.getIdContaminante());
        existente.setIdUsuario(dto.getIdUsuario());

        return toDTO(repositoryPort.guardar(existente));
    }

    // =========================
    // VALIDACIONES
    // =========================
    private void validarMedicion(MedicionDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("La medición no puede ser nula");
        }

        if (dto.getPh() == null) {
            throw new IllegalArgumentException("El pH es obligatorio");
        }

        if (dto.getPh() < 0 || dto.getPh() > 14) {
            throw new IllegalArgumentException("El pH debe estar entre 0 y 14");
        }

        if (dto.getTemperatura() == null) {
            throw new IllegalArgumentException("La temperatura es obligatoria");
        }

        if (dto.getTemperatura() < -50 || dto.getTemperatura() > 100) {
            throw new IllegalArgumentException("Temperatura fuera de rango realista");
        }

        if (dto.getIdRecurso() == null || dto.getIdRecurso() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un recurso válido");
        }

        if (dto.getIdContaminante() == null || dto.getIdContaminante() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un contaminante válido");
        }

        if (dto.getIdUsuario() == null || dto.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Usuario inválido");
        }
    }

    // =========================
    // MAPPERS
    // =========================
    private Medicion toEntity(MedicionDTO dto) {
        return new Medicion(
                dto.getId(),
                dto.getPh(),
                dto.getTemperatura(),
                dto.getFecha(),
                dto.getIdUsuario(),
                dto.getIdRecurso(),
                dto.getIdContaminante()
        );
    }

    private MedicionDTO toDTO(Medicion m) {
        return new MedicionDTO(
                m.getId(),
                m.getPh(),
                m.getTemperatura(),
                m.getFecha(),
                m.getIdUsuario(),
                m.getIdRecurso(),
                m.getIdContaminante()
        );
    }
}