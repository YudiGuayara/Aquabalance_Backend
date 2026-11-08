package com.AquaBalance.events.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.AquaBalance.events.domain.NivelAlerta;

/** El usuario NO viaja en el cuerpo: se toma del token JWT. Si un cliente viejo lo envía, se ignora. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertaRequest {
    private NivelAlerta nivel;
    private String mensaje;
    private Long idEvento;

    public NivelAlerta getNivel() { return nivel; }
    public void setNivel(NivelAlerta nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }
}