package com.AquaBalance.events.domain;

import java.time.LocalDateTime;

public class Alerta {
    private Long id;
    private LocalDateTime fecha;
    private NivelAlerta nivel;
    private String mensaje;
    private Long idUsuario;
    private Long idEvento;

    public Alerta() {}

    public Alerta(Long id, LocalDateTime fecha, NivelAlerta nivel, String mensaje, Long idUsuario, Long idEvento) {
        this.id = id;
        this.fecha = fecha;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
        this.idEvento = idEvento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public NivelAlerta getNivel() { return nivel; }
    public void setNivel(NivelAlerta nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }
}