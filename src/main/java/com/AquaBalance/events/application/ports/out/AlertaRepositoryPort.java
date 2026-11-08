package com.AquaBalance.events.application.ports.out;


import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.NivelAlerta;
import java.util.List;
import java.util.Optional;

public interface AlertaRepositoryPort {
    Alerta guardar(Alerta alerta);
    Optional<Alerta> buscarPorId(Long id);
    List<Alerta> listarTodos();
    List<Alerta> buscarPorNivel(NivelAlerta nivel);
    List<Alerta> buscarPorEvento(Long idEvento);
    void eliminar(Long id);
}
