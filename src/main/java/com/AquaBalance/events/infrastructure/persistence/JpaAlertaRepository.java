package com.AquaBalance.events.infrastructure.persistence;


import com.AquaBalance.events.domain.NivelAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaAlertaRepository extends JpaRepository<AlertaEntity, Long> {
    List<AlertaEntity> findByNivel(NivelAlerta nivel);
    List<AlertaEntity> findByIdEvento(Long idEvento);
}