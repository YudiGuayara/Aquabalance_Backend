package com.AquaBalance.notifications.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository
        extends JpaRepository<NotificacionEntity, Long> {

    List<NotificacionEntity> findTop50ByOrderByFechaDesc();

    long countByLeidaFalse();

    @Modifying
    @Query("UPDATE NotificacionEntity n SET n.leida = true")
    void marcarTodasLeidas();
}
