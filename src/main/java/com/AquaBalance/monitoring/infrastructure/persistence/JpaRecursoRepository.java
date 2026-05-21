package com.AquaBalance.monitoring.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRecursoRepository extends JpaRepository<RecursoEntity, Long> {
}