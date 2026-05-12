package com.AquaBalance.monitoreo.application;

import com.AquaBalance.monitoreo.application.ports.in.GestionarRecursoUseCase;
import com.AquaBalance.monitoreo.application.ports.out.RecursoRepositoryPort;
import com.AquaBalance.monitoreo.domain.Recurso;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecursoService implements GestionarRecursoUseCase {

    private final RecursoRepositoryPort repositoryPort;

    public RecursoService(RecursoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    // =========================
    // CREAR
    // =========================
    @Override
    public RecursoDTO crear(RecursoDTO dto) {

        validarRecurso(dto);

        Recurso recurso = toEntity(dto);
        return toDTO(repositoryPort.guardar(recurso));
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Override
    public RecursoDTO actualizar(Long id, RecursoDTO dto) {

        repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Recurso no encontrado con id: " + id));

        validarRecurso(dto);

        Recurso recurso = toEntity(dto);
        recurso.setId(id);

        return toDTO(repositoryPort.guardar(recurso));
    }

    // =========================
    // BUSCAR
    // =========================
    @Override
    public RecursoDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Recurso no encontrado con id: " + id));
    }

    // =========================
    // LISTAR
    // =========================
    @Override
    public List<RecursoDTO> listarTodos() {
        return repositoryPort.listarTodos()
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
                        new ResourceNotFoundException("Recurso no encontrado con id: " + id));

        repositoryPort.eliminar(id);
    }

    // =========================
    // VALIDACIONES
    // =========================
    private void validarRecurso(RecursoDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio");
        }

        if (dto.getNombre().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
        }

        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }

        if (dto.getUbicacion() == null || dto.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }

        if (dto.getLatitud() == null) {
            throw new IllegalArgumentException("La latitud es obligatoria");
        }

        if (dto.getLongitud() == null) {
            throw new IllegalArgumentException("La longitud es obligatoria");
        }

        // Validación geográfica básica
        if (dto.getLatitud() < -90 || dto.getLatitud() > 90) {
            throw new IllegalArgumentException("Latitud inválida");
        }

        if (dto.getLongitud() < -180 || dto.getLongitud() > 180) {
            throw new IllegalArgumentException("Longitud inválida");
        }
    }

    // =========================
    // MAPPERS
    // =========================
    private Recurso toEntity(RecursoDTO dto) {
        return new Recurso(
                dto.getId(),
                dto.getNombre(),
                dto.getTipo(),
                dto.getUbicacion(),
                dto.getLatitud(),
                dto.getLongitud()
        );
    }

    private RecursoDTO toDTO(Recurso recurso) {
        return new RecursoDTO(
                recurso.getId(),
                recurso.getNombre(),
                recurso.getTipo(),
                recurso.getUbicacion(),
                recurso.getLatitud(),
                recurso.getLongitud()
        );
    }
}