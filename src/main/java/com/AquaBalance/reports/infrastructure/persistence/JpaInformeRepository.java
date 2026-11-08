package com.AquaBalance.reports.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInformeRepository extends JpaRepository<InformeEntity, Long> {
}