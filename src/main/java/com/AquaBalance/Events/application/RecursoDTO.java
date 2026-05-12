package com.AquaBalance.monitoreo.application;

import com.AquaBalance.monitoreo.domain.TipoRecurso;

public class RecursoDTO {
    private Long id;
    private String nombre;
    private TipoRecurso tipo;
    private String ubicacion;
    private Double latitud;
    private Double longitud;

    public RecursoDTO() {}

    public RecursoDTO(Long id, String nombre, TipoRecurso tipo, String ubicacion, Double latitud, Double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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