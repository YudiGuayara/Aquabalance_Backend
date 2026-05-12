package com.AquaBalance.monitoreo.infrastructure.web.dto;

import com.AquaBalance.monitoreo.domain.NivelContaminante;

public class ContaminanteResponse {
    private Long id;
    private String nombre;
    private Double carga;
    private NivelContaminante nivel;
    private String fuenteOrigen;

    public ContaminanteResponse() {}

    public ContaminanteResponse(Long id, String nombre, Double carga, NivelContaminante nivel, String fuenteOrigen) {
        this.id = id;
        this.nombre = nombre;
        this.carga = carga;
        this.nivel = nivel;
        this.fuenteOrigen = fuenteOrigen;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Double getCarga() { return carga; }
    public NivelContaminante getNivel() { return nivel; }
    public String getFuenteOrigen() { return fuenteOrigen; }
}