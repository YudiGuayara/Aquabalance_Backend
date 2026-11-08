package com.AquaBalance.events.application.ports.out;

import com.AquaBalance.events.domain.Evento;
import java.util.List;
import java.util.Optional;

public interface EventoRepositoryPort {
    Evento guardar(Evento evento);
    Optional<Evento> buscarPorId(Long id);
    List<Evento> listarTodos();
    List<Evento> buscarPorRecurso(Long idRecurso);
    void eliminar(Long id);
}