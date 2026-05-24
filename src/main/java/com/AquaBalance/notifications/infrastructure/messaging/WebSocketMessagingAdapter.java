package com.AquaBalance.notifications.infrastructure.messaging;

import com.AquaBalance.notifications.application.ports.out.NotificacionMessagingPort;
import com.AquaBalance.notifications.domain.Notificacion;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessagingAdapter implements NotificacionMessagingPort {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagingAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        messagingTemplate.convertAndSend("/topic/notificaciones", notificacion);
    }
}