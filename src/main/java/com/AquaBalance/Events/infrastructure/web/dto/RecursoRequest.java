package com.AquaBalance.monitoreo.infrastructure.web.dto;

import com.AquaBalance.monitoreo.domain.TipoRecurso;

public class RecursoRequest {
    private String nombre;
    private TipoRecurso tipo;
    private String ubicacion;
    private Double latitud;
    private Double longitud;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoRecurso getTipo() { return tipo; }
    public void setTipo(TipoRecurso tipo) { this.tipo = tipo; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}