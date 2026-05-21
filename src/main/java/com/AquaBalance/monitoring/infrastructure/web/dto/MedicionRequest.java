package com.AquaBalance.monitoring.infrastructure.web.dto;

public class MedicionRequest {
    private Double ph;
    private Double temperatura;
    private Long idUsuario;
    private Long idRecurso;
    private Long idContaminante;

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Long idRecurso) { this.idRecurso = idRecurso; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }
}