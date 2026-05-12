package com.AquaBalance.monitoreo.application.ports.out;

import com.AquaBalance.monitoreo.domain.Recurso;
import java.util.List;
import java.util.Optional;

public interface RecursoRepositoryPort {
    Recurso guardar(Recurso recurso);
    Optional<Recurso> buscarPorId(Long id);
    List<Recurso> listarTodos();
    void eliminar(Long id);
}