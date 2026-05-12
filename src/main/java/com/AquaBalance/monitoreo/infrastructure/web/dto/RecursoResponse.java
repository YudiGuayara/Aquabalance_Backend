package com.AquaBalance.monitoreo.infrastructure.web.dto;

import com.AquaBalance.monitoreo.domain.TipoRecurso;

public class RecursoResponse {
    private Long id;
    private String nombre;
    private TipoRecurso tipo;
    private String ubicacion;
    private Double latitud;
    private Double longitud;

    public RecursoResponse() {}

    public RecursoResponse(Long id, String nombre, TipoRecurso tipo, String ubicacion, Double latitud, Double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoRecurso getTipo() { return tipo; }
    public String getUbicacion() { return ubicacion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
}