package com.AquaBalance.monitoreo.application.ports.out;

import com.AquaBalance.monitoreo.domain.Contaminante;
import java.util.List;
import java.util.Optional;

public interface ContaminanteRepositoryPort {
    Contaminante guardar(Contaminante contaminante);
    Optional<Contaminante> buscarPorId(Long id);
    List<Contaminante> listarTodos();
    void eliminar(Long id);
}