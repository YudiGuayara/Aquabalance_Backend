package com.AquaBalance.monitoreo.application.ports.out;

import com.AquaBalance.monitoreo.domain.Medicion;
import java.util.List;
import java.util.Optional;

public interface MedicionRepositoryPort {
    Medicion guardar(Medicion medicion);
    Optional<Medicion> buscarPorId(Long id);
    List<Medicion> listarTodos();
    List<Medicion> buscarPorRecurso(Long idRecurso);
    void eliminar(Long id);
}