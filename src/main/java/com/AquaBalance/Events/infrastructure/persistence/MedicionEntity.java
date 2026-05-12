package com.AquaBalance.monitoreo.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mediciones")
public class MedicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double ph;
    private Double temperatura;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_recurso", nullable = false)
    private Long idRecurso;

    @Column(name = "id_contaminante", nullable = false)
    private Long idContaminante;

    public MedicionEntity() {}

    public MedicionEntity(Long id, Double ph, Double temperatura, LocalDateTime fecha, Long idUsuario, Long idRecurso, Long idContaminante) {
        this.id = id;
        this.ph = ph;
        this.temperatura = temperatura;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idRecurso = idRecurso;
        this.idContaminante = idContaminante;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Long idRecurso) { this.idRecurso = idRecurso; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }
}