package com.AquaBalance.monitoring.application.ports.out;

import com.AquaBalance.monitoring.domain.Contaminante;
import java.util.List;
import java.util.Optional;

public interface ContaminanteRepositoryPort {
    Contaminante guardar(Contaminante contaminante);
    Optional<Contaminante> buscarPorId(Long id);
    List<Contaminante> listarTodos();
    void eliminar(Long id);
}