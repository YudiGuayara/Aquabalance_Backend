package com.AquaBalance.user.infrastructure.web.dto;

public class MensajeResponse {

    private String mensaje;

    public MensajeResponse() {}
    public MensajeResponse(String mensaje) { this.mensaje = mensaje; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
