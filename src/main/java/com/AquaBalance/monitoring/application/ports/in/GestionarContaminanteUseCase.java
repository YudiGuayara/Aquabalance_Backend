package com.AquaBalance.monitoring.application.ports.in;

import com.AquaBalance.monitoring.application.ContaminanteDTO;
import java.util.List;

public interface GestionarContaminanteUseCase {
    ContaminanteDTO crear(ContaminanteDTO dto);
    ContaminanteDTO actualizar(Long id, ContaminanteDTO dto);
    ContaminanteDTO buscarPorId(Long id);
    List<ContaminanteDTO> listarTodos();
    void eliminar(Long id);
}