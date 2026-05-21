package com.AquaBalance.monitoring.domain;

import java.util.List;
import java.util.Optional;

public interface ContaminanteRepository {
    Contaminante guardar(Contaminante contaminante);
    Optional<Contaminante> buscarPorId(Long id);
    List<Contaminante> listarTodos();
    void eliminar(Long id);
}