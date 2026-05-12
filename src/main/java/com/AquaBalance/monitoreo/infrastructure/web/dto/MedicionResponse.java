package com.AquaBalance.monitoreo.infrastructure.web.dto;

import java.time.LocalDateTime;

public class MedicionResponse {
    private Long id;
    private Double ph;
    private Double temperatura;
    private LocalDateTime fecha;
    private Long idUsuario;
    private Long idRecurso;
    private Long idContaminante;

    public MedicionResponse() {}

    public MedicionResponse(Long id, Double ph, Double temperatura, LocalDateTime fecha, Long idUsuario, Long idRecurso, Long idContaminante) {
        this.id = id;
        this.ph = ph;
        this.temperatura = temperatura;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idRecurso = idRecurso;
        this.idContaminante = idContaminante;
    }

    public Long getId() { return id; }
    public Double getPh() { return ph; }
    public Double getTemperatura() { return temperatura; }
    public LocalDateTime getFecha() { return fecha; }
    public Long getIdUsuario() { return idUsuario; }
    public Long getIdRecurso() { return idRecurso; }
    public Long getIdContaminante() { return idContaminante; }
}