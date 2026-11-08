package com.AquaBalance.events.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaEventoRepository extends JpaRepository<EventoEntity, Long> {
    List<EventoEntity> findByIdRecurso(Long idRecurso);
}
