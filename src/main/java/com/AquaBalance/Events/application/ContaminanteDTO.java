package com.AquaBalance.monitoreo.application;

import com.AquaBalance.monitoreo.domain.NivelContaminante;

public class ContaminanteDTO {
    private Long id;
    private String nombre;
    private Double carga;
    private NivelContaminante nivel;
    private String fuenteOrigen;

    public ContaminanteDTO() {}

    public ContaminanteDTO(Long id, String nombre, Double carga, NivelContaminante nivel, String fuenteOrigen) {
        this.id = id;
        this.nombre = nombre;
        this.carga = carga;
        this.nivel = nivel;
        this.fuenteOrigen = fuenteOrigen;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getCarga() { return carga; }
    public void setCarga(Double carga) { this.carga = carga; }

    public NivelContaminante getNivel() { return nivel; }
    public void setNivel(NivelContaminante nivel) { this.nivel = nivel; }

    public String getFuenteOrigen() { return fuenteOrigen; }
    public void setFuenteOrigen(String fuenteOrigen) { this.fuenteOrigen = fuenteOrigen; }
}