package com.AquaBalance.notifications.application;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import com.AquaBalance.notifications.application.ports.out.NotificacionMessagingPort;
import com.AquaBalance.notifications.domain.Notificacion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NotificacionService implements GestionarNotificacionUseCase {

    private final NotificacionMessagingPort messagingPort;
    private final NotificacionEmailPort     emailPort;

    private final List<Notificacion> historial =
            Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong contador = new AtomicLong(1);

    public NotificacionService(NotificacionMessagingPort messagingPort,
                               NotificacionEmailPort emailPort) {
        this.messagingPort = messagingPort;
        this.emailPort     = emailPort;
    }

    @Override
    public void notificar(Notificacion notificacion) {

        notificacion.setId(contador.getAndIncrement());

        historial.add(0, notificacion);
        if (historial.size() > 50) {
            historial.remove(historial.size() - 1);
        }

        // Enviar por WebSocket
        messagingPort.enviar(notificacion);

        // Enviar email solo si ALTA o MEDIA
        String nivel = notificacion.getNivel();
        if (nivel != null &&
                (nivel.equalsIgnoreCase("ALTA") || nivel.equalsIgnoreCase("MEDIA"))) {
            emailPort.enviar(notificacion);
        }

        System.out.println("🔔 Notificación enviada: " + notificacion.getTitulo());
    }

    @Override
    public void notificarAlerta(String mensaje, String nivel) {
        notificar(new Notificacion(
                "ALERTA",
                "Nueva alerta — Nivel " + nivel,
                mensaje,
                nivel
        ));
    }

    @Override
    public void notificarInforme(String tituloInforme) {
        notificar(new Notificacion(
                "INFORME",
                "Informe generado",
                "Se ha generado el informe: " + tituloInforme,
                "INFO"
        ));
    }

    @Override
    public void notificarMedicion(String recurso, String contaminante, double valor) {
        notificar(new Notificacion(
                "MEDICION",
                "Medición registrada — " + recurso,
                "Contaminante: " + contaminante + " | Valor: " + valor,
                "BAJA"
        ));
    }

    @Override
    public List<Notificacion> obtenerHistorial() {
        return Collections.unmodifiableList(historial);
    }

    @Override
    public long contarNoLeidas() {
        return historial.stream().filter(n -> !n.isLeida()).count();
    }

    @Override
    public void marcarTodasLeidas() {
        historial.forEach(n -> n.setLeida(true));
    }

    @Override
    public void marcarLeida(Long id) {
        historial.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .ifPresent(n -> n.setLeida(true));
    }
}