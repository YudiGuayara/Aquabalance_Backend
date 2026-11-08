package com.AquaBalance.monitoring.infrastructure.web.dto;

import java.time.LocalDateTime;

public class MedicionResponse {
    private Long id;
    private Double ph;
    private Double temperatura;
    private LocalDateTime fecha;
    private Long idUsuario;
    private Long idRecurso;
    private String nombreRecurso;
    private Long idContaminante;
    private String nombreContaminante;

    public MedicionResponse() {}

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

    public String getNombreRecurso() { return nombreRecurso; }
    public void setNombreRecurso(String nombreRecurso) { this.nombreRecurso = nombreRecurso; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }

    public String getNombreContaminante() { return nombreContaminante; }
    public void setNombreContaminante(String nombreContaminante) { this.nombreContaminante = nombreContaminante; }
}