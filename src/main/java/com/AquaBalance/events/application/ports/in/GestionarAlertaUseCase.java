package com.AquaBalance.events.application.ports.in;

import com.AquaBalance.events.application.AlertaDTO;
import com.AquaBalance.events.domain.NivelAlerta;
import java.util.List;

public interface GestionarAlertaUseCase {
    AlertaDTO crear(AlertaDTO dto);
    AlertaDTO buscarPorId(Long id);
    List<AlertaDTO> listarTodos();
    List<AlertaDTO> listarPorNivel(NivelAlerta nivel);
    List<AlertaDTO> listarPorEvento(Long idEvento);
    AlertaDTO actualizar(Long id, AlertaDTO dto);
    void eliminar(Long id);
}