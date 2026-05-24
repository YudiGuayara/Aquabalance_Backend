package com.AquaBalance.notifications.application.ports.out;

import com.AquaBalance.notifications.domain.Notificacion;

public interface NotificacionMessagingPort {
    void enviar(Notificacion notificacion);
}