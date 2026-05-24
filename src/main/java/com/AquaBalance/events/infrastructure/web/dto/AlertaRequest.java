package com.AquaBalance.events.infrastructure.web.dto;


import com.AquaBalance.events.domain.NivelAlerta;

public class AlertaRequest {
    private NivelAlerta nivel;
    private String mensaje;
    private Long idUsuario;
    private Long idEvento;

    public NivelAlerta getNivel() { return nivel; }
    public void setNivel(NivelAlerta nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }
}