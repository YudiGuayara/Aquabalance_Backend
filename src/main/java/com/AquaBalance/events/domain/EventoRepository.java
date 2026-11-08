package com.AquaBalance.events.domain;


import java.util.List;
import java.util.Optional;

public interface EventoRepository {
    Evento guardar(Evento evento);
    Optional<Evento> buscarPorId(Long id);
    List<Evento> listarTodos();
    List<Evento> buscarPorRecurso(Long idRecurso);
    void eliminar(Long id);
}