package com.AquaBalance.reports.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class InformeRequest {

    private String titulo;
    private String descripcion;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private Long recursoId;
    private Long contaminanteId;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Long getRecursoId() { return recursoId; }
    public void setRecursoId(Long recursoId) { this.recursoId = recursoId; }

    public Long getContaminanteId() { return contaminanteId; }
    public void setContaminanteId(Long contaminanteId) { this.contaminanteId = contaminanteId; }
}