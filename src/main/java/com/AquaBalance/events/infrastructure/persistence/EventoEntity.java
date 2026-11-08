package com.AquaBalance.events.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
public class EventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String magnitud;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "id_contaminante", nullable = false)
    private Long idContaminante;

    @Column(name = "id_recurso", nullable = false)
    private Long idRecurso;

    public EventoEntity() {}

    public EventoEntity(Long id, String descripcion, String magnitud, LocalDateTime fecha, Long idContaminante, Long idRecurso) {
        this.id = id;
        this.descripcion = descripcion;
        this.magnitud = magnitud;
        this.fecha = fecha;
        this.idContaminante = idContaminante;
        this.idRecurso = idRecurso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMagnitud() { return magnitud; }
    public void setMagnitud(String magnitud) { this.magnitud = magnitud; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }

    public Long getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Long idRecurso) { this.idRecurso = idRecurso; }
}