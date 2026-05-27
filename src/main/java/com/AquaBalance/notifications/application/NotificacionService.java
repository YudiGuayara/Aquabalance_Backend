package com.AquaBalance.notifications.application;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import com.AquaBalance.notifications.application.ports.out.NotificacionMessagingPort;
import com.AquaBalance.notifications.domain.Notificacion;
import com.AquaBalance.notifications.infrastructure.persistence.NotificacionEntity;
import com.AquaBalance.notifications.infrastructure.persistence.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService implements GestionarNotificacionUseCase {

    private final NotificacionMessagingPort messagingPort;
    private final NotificacionEmailPort     emailPort;
    private final NotificacionRepository    repository;

    public NotificacionService(NotificacionMessagingPort messagingPort,
                               NotificacionEmailPort emailPort,
                               NotificacionRepository repository) {
        this.messagingPort = messagingPort;
        this.emailPort     = emailPort;
        this.repository    = repository;
    }

    // ── Notificar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificar(Notificacion notificacion) {
        // 1. Guardar en BD
        NotificacionEntity entity = toEntity(notificacion);
        entity = repository.save(entity);
        notificacion.setId(entity.getId());
        notificacion.setLeida(false);

        // 2. Enviar por WebSocket (siempre)
        messagingPort.enviar(notificacion);

        // 3. Email solo si nivel es ALTA o MEDIA
        String nivel = notificacion.getNivel();
        System.out.println("🔔 Notificación guardada: " + notificacion.getTitulo()
                + " | Nivel: " + nivel);

        if (nivel != null &&
                (nivel.equalsIgnoreCase("ALTA") || nivel.equalsIgnoreCase("MEDIA"))) {
            System.out.println("📤 Enviando email para nivel: " + nivel);
            emailPort.enviar(notificacion);
        } else {
            System.out.println("⏭️  Nivel '" + nivel + "' no requiere email.");
        }
    }

    @Override
    public void notificarAlerta(String mensaje, String nivel) {
        notificar(new Notificacion(
                "ALERTA",
                "Nueva alerta — Nivel " + nivel,
                mensaje,
                nivel));
    }

    @Override
    public void notificarInforme(String tituloInforme) {
        notificar(new Notificacion(
                "INFORME",
                "Informe generado",
                "Se ha generado el informe: " + tituloInforme,
                "INFO"));
    }

    @Override
    public void notificarMedicion(String recurso, String contaminante, double valor) {
        notificar(new Notificacion(
                "MEDICION",
                "Medición registrada — " + recurso,
                "Contaminante: " + contaminante + " | Valor: " + valor,
                "BAJA"));
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerHistorial() {
        return repository.findTop50ByOrderByFechaDesc()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        return repository.countByLeidaFalse();
    }

    // ── Marcar leídas ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void marcarTodasLeidas() {
        repository.marcarTodasLeidas();
    }

    @Override
    @Transactional
    public void marcarLeida(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setLeida(true);
            repository.save(n);
        });
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private NotificacionEntity toEntity(Notificacion n) {
        NotificacionEntity e = new NotificacionEntity();
        e.setTipo(n.getTipo());
        e.setTitulo(n.getTitulo());
        e.setMensaje(n.getMensaje());
        e.setNivel(n.getNivel());
        e.setLeida(false);
        e.setFecha(n.getFecha() != null ? n.getFecha() : LocalDateTime.now());
        return e;
    }

    private Notificacion toDomain(NotificacionEntity e) {
        Notificacion n = new Notificacion();
        n.setId(e.getId());
        n.setTipo(e.getTipo());
        n.setTitulo(e.getTitulo());
        n.setMensaje(e.getMensaje());
        n.setNivel(e.getNivel());
        n.setLeida(e.isLeida());
        n.setFecha(e.getFecha());
        return n;
    }
}