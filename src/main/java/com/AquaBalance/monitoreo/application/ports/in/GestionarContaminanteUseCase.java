package com.AquaBalance.monitoreo.application.ports.in;

import com.AquaBalance.monitoreo.application.ContaminanteDTO;
import java.util.List;

public interface GestionarContaminanteUseCase {
    ContaminanteDTO crear(ContaminanteDTO dto);
    ContaminanteDTO actualizar(Long id, ContaminanteDTO dto);
    ContaminanteDTO buscarPorId(Long id);
    List<ContaminanteDTO> listarTodos();
    void eliminar(Long id);
}