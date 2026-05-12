package com.AquaBalance.monitoreo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaContaminanteRepository extends JpaRepository<ContaminanteEntity, Long> {
}