package com.AquaBalance.reports.domain;

import java.time.LocalDateTime;

public class Informe {

    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long recursoId;
    private Long contaminanteId;

    public Informe() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime f) { this.fechaGeneracion = f; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime f) { this.fechaInicio = f; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime f) { this.fechaFin = f; }
    public Long getRecursoId() { return recursoId; }
    public void setRecursoId(Long recursoId) { this.recursoId = recursoId; }
    public Long getContaminanteId() { return contaminanteId; }
    public void setContaminanteId(Long v) { this.contaminanteId = v; }
}