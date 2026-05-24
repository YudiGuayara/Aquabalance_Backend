package com.AquaBalance.events.domain;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository {
    Alerta guardar(Alerta alerta);
    Optional<Alerta> buscarPorId(Long id);
    List<Alerta> listarTodos();
    List<Alerta> buscarPorNivel(NivelAlerta nivel);
    List<Alerta> buscarPorEvento(Long idEvento);
    void eliminar(Long id);
}