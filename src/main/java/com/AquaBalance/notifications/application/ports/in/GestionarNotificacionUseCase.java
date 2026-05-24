package com.AquaBalance.notifications.application.ports.in;

import com.AquaBalance.notifications.domain.Notificacion;
import java.util.List;

public interface GestionarNotificacionUseCase {
    void notificar(Notificacion notificacion);
    void notificarAlerta(String mensaje, String nivel);
    void notificarInforme(String tituloInforme);
    void notificarMedicion(String recurso, String contaminante, double valor);
    List<Notificacion> obtenerHistorial();
    long contarNoLeidas();
    void marcarTodasLeidas();
    void marcarLeida(Long id);
}