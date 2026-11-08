package com.AquaBalance.monitoring.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** El usuario NO viaja en el cuerpo: se toma del token JWT. Si un cliente viejo lo envía, se ignora. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicionRequest {
    private Double ph;
    private Double temperatura;
    private Long idRecurso;
    private Long idContaminante;

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Long getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Long idRecurso) { this.idRecurso = idRecurso; }

    public Long getIdContaminante() { return idContaminante; }
    public void setIdContaminante(Long idContaminante) { this.idContaminante = idContaminante; }
}