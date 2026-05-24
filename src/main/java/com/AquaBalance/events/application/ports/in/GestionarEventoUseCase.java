package com.AquaBalance.events.application.ports.in;

import com.AquaBalance.events.application.EventoDTO;
import java.util.List;

public interface GestionarEventoUseCase {
    EventoDTO crear(EventoDTO dto);
    EventoDTO buscarPorId(Long id);
    List<EventoDTO> listarTodos();
    List<EventoDTO> listarPorRecurso(Long idRecurso);
    EventoDTO actualizar(Long id, EventoDTO dto);
    void eliminar(Long id);

}