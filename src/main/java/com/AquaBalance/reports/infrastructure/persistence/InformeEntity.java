package com.AquaBalance.reports.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "informes")
public class InformeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    @Column(name = "contaminante_id", nullable = false)
    private Long contaminanteId;

    // ✅ Campos opcionales
    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "id_alerta")
    private Long idAlerta;

    @Column(name = "id_medicion")
    private Long idMedicion;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "observacion")
    private String observacion;

    public InformeEntity() {}

    // =========================
    // Valores automáticos
    // =========================
    @PrePersist
    public void prePersist() {

        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }

        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        if (observacion == null || observacion.isBlank()) {
            observacion = "Sin observaciones";
        }
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(Long recursoId) {
        this.recursoId = recursoId;
    }

    public Long getContaminanteId() {
        return contaminanteId;
    }

    public void setContaminanteId(Long contaminanteId) {
        this.contaminanteId = contaminanteId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Long idAlerta) {
        this.idAlerta = idAlerta;
    }

    public Long getIdMedicion() {
        return idMedicion;
    }

    public void setIdMedicion(Long idMedicion) {
        this.idMedicion = idMedicion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}