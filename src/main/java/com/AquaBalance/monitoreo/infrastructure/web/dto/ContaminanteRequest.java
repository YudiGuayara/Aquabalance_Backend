package com.AquaBalance.monitoreo.infrastructure.web.dto;

import com.AquaBalance.monitoreo.domain.NivelContaminante;

public class ContaminanteRequest {
    private String nombre;
    private Double carga;
    private NivelContaminante nivel;
    private String fuenteOrigen;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getCarga() { return carga; }
    public void setCarga(Double carga) { this.carga = carga; }

    public NivelContaminante getNivel() { return nivel; }
    public void setNivel(NivelContaminante nivel) { this.nivel = nivel; }

    public String getFuenteOrigen() { return fuenteOrigen; }
    public void setFuenteOrigen(String fuenteOrigen) { this.fuenteOrigen = fuenteOrigen; }
}