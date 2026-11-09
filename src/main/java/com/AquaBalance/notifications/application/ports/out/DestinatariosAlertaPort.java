package com.AquaBalance.notifications.application.ports.out;

import java.util.List;

/** Quién debe recibir por correo las alertas del sistema. */
public interface DestinatariosAlertaPort {

    /** Correos de los operadores activos con la cuenta verificada (puede estar vacía). */
    List<String> emailsDeOperadores();
}
