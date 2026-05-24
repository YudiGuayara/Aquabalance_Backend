package com.AquaBalance.events.infrastructure.web.dto;

public class EventoRequest {
    private String descripcion;
    private String magnitud;
    private Long idContaminante;
    private Long idRecurso;

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMagnitud() { return magnitud; }
    public void setMagnitud(String magnitud) { this.magnitud = magnitud; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }

    public Long getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Long idRecurso) { this.idRecurso = idRecurso; }
}