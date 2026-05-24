package com.AquaBalance.events.infrastructure.web.dto;


import com.AquaBalance.events.domain.NivelAlerta;
import java.time.LocalDateTime;

public class AlertaResponse {
    private Long id;
    private LocalDateTime fecha;
    private NivelAlerta nivel;
    private String mensaje;
    private Long idUsuario;
    private Long idEvento;

    public AlertaResponse() {}

    public AlertaResponse(Long id, LocalDateTime fecha, NivelAlerta nivel, String mensaje, Long idUsuario, Long idEvento) {
        this.id = id;
        this.fecha = fecha;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
        this.idEvento = idEvento;
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public NivelAlerta getNivel() { return nivel; }
    public String getMensaje() { return mensaje; }
    public Long getIdUsuario() { return idUsuario; }
    public Long getIdEvento() { return idEvento; }
}
