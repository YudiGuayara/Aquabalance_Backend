package com.AquaBalance.monitoreo.application;

import com.AquaBalance.monitoreo.application.ports.in.GestionarContaminanteUseCase;
import com.AquaBalance.monitoreo.application.ports.out.ContaminanteRepositoryPort;
import com.AquaBalance.monitoreo.domain.Contaminante;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaminanteService implements GestionarContaminanteUseCase {

    private final ContaminanteRepositoryPort repositoryPort;

    public ContaminanteService(ContaminanteRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    // =========================
    // CREAR
    // =========================
    @Override
    public ContaminanteDTO crear(ContaminanteDTO dto) {

        validarContaminante(dto);

        return toDTO(repositoryPort.guardar(toEntity(dto)));
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Override
    public ContaminanteDTO actualizar(Long id, ContaminanteDTO dto) {

        repositoryPort.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contaminante no encontrado con id: " + id));

        validarContaminante(dto);

        Contaminante contaminante = toEntity(dto);
        contaminante.setId(id);

        return toDTO(repositoryPort.guardar(contaminante));
    }

    // =========================
    // BUSCAR
    // =========================
    @Override
    public ContaminanteDTO buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contaminante no encontrado con id: " + id));
    }

    // =========================
    // LISTAR
    // =========================
    @Override
    public List<ContaminanteDTO> listarTodos() {
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
                        new ResourceNotFoundException("Contaminante no encontrado con id: " + id));

        repositoryPort.eliminar(id);
    }

    // =========================
    // VALIDACIONES
    // =========================
    private void validarContaminante(ContaminanteDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("El contaminante no puede ser nulo");
        }

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dto.getNombre().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
        }

        if (dto.getCarga() == null) {
            throw new IllegalArgumentException("La carga es obligatoria");
        }

        if (dto.getCarga() < 0) {
            throw new IllegalArgumentException("La carga no puede ser negativa");
        }

        if (dto.getNivel() == null) {
            throw new IllegalArgumentException("El nivel es obligatorio");
        }

        if (dto.getFuenteOrigen() == null || dto.getFuenteOrigen().trim().isEmpty()) {
            throw new IllegalArgumentException("La fuente de origen es obligatoria");
        }
    }

    // =========================
    // MAPPERS
    // =========================
    private Contaminante toEntity(ContaminanteDTO dto) {
        return new Contaminante(
                dto.getId(),
                dto.getNombre(),
                dto.getCarga(),
                dto.getNivel(),
                dto.getFuenteOrigen()
        );
    }

    private ContaminanteDTO toDTO(Contaminante c) {
        return new ContaminanteDTO(
                c.getId(),
                c.getNombre(),
                c.getCarga(),
                c.getNivel(),
                c.getFuenteOrigen()
        );
    }
}