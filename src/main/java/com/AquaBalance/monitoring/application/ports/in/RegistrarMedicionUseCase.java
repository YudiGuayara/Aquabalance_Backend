package com.AquaBalance.monitoring.application.ports.in;

import com.AquaBalance.monitoring.application.MedicionDTO;
import java.util.List;

public interface RegistrarMedicionUseCase {
    MedicionDTO registrar(MedicionDTO dto);
    MedicionDTO actualizar(Long id, MedicionDTO dto);
    MedicionDTO buscarPorId(Long id);
    List<MedicionDTO> listarTodos();
    List<MedicionDTO> listarPorRecurso(Long idRecurso);
    void eliminar(Long id);
}