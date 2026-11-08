package com.AquaBalance.monitoring.application.ports.in;

import com.AquaBalance.monitoring.application.RecursoDTO;
import java.util.List;

public interface GestionarRecursoUseCase {
    RecursoDTO crear(RecursoDTO dto);
    RecursoDTO actualizar(Long id, RecursoDTO dto);
    RecursoDTO buscarPorId(Long id);
    List<RecursoDTO> listarTodos();
    void eliminar(Long id);
}
