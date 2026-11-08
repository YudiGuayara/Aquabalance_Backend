package com.AquaBalance.events.infrastructure.web.dto;


import java.time.LocalDateTime;

public class EventoResponse {
    private Long id;
    private String descripcion;
    private String magnitud;
    private LocalDateTime fecha;
    private Long idContaminante;
    private Long idRecurso;

    public EventoResponse() {}

    public EventoResponse(Long id, String descripcion, String magnitud, LocalDateTime fecha, Long idContaminante, Long idRecurso) {
        this.id = id;
        this.descripcion = descripcion;
        this.magnitud = magnitud;
        this.fecha = fecha;
        this.idContaminante = idContaminante;
        this.idRecurso = idRecurso;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public String getMagnitud() { return magnitud; }
    public LocalDateTime getFecha() { return fecha; }
    public Long getIdContaminante() { return idContaminante; }
    public Long getIdRecurso() { return idRecurso; }
}