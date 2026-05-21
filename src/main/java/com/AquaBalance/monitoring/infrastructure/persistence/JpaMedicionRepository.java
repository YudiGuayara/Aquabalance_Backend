package com.AquaBalance.monitoring.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaMedicionRepository extends JpaRepository<MedicionEntity, Long> {
    List<MedicionEntity> findByIdRecurso(Long idRecurso);
}