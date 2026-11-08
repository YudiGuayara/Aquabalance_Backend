package com.AquaBalance.monitoring.domain;

import java.util.List;
import java.util.Optional;

public interface RecursoRepository {
    Recurso guardar(Recurso recurso);
    Optional<Recurso> buscarPorId(Long id);
    List<Recurso> listarTodos();
    void eliminar(Long id);
}
