package com.AquaBalance.reports.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class InformeResponse {

    private Long id;
    private String titulo;
    private String descripcion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaGeneracion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    private Long recursoId;
    private String nombreRecurso;
    private Long contaminanteId;
    private String nombreContaminante;
    private EstadisticasResponse estadisticas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime v) { this.fechaGeneracion = v; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime v) { this.fechaInicio = v; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime v) { this.fechaFin = v; }
    public Long getRecursoId() { return recursoId; }
    public void setRecursoId(Long v) { this.recursoId = v; }
    public String getNombreRecurso() { return nombreRecurso; }
    public void setNombreRecurso(String v) { this.nombreRecurso = v; }
    public Long getContaminanteId() { return contaminanteId; }
    public void setContaminanteId(Long v) { this.contaminanteId = v; }
    public String getNombreContaminante() { return nombreContaminante; }
    public void setNombreContaminante(String v) { this.nombreContaminante = v; }
    public EstadisticasResponse getEstadisticas() { return estadisticas; }
    public void setEstadisticas(EstadisticasResponse v) { this.estadisticas = v; }
}