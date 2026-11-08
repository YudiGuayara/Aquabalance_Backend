package com.AquaBalance.notifications.domain;

import java.time.LocalDateTime;

public class Notificacion {

    private Long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String nivel;
    private boolean leida;
    private LocalDateTime fecha;

    public Notificacion() {}

    public Notificacion(String tipo, String titulo, String mensaje, String nivel) {
        this.tipo    = tipo;
        this.titulo  = titulo;
        this.mensaje = mensaje;
        this.nivel   = nivel;
        this.leida   = false;
        this.fecha   = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}