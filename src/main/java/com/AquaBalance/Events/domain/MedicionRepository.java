package com.AquaBalance.monitoreo.domain;

import java.util.List;
import java.util.Optional;

public interface MedicionRepository {
    Medicion guardar(Medicion medicion);
    Optional<Medicion> buscarPorId(Long id);
    List<Medicion> listarTodos();
    List<Medicion> buscarPorRecurso(Long idRecurso);
    void eliminar(Long id);
}