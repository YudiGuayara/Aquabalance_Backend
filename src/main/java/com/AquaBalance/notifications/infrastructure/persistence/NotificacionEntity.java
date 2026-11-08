package com.AquaBalance.notifications.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private String nivel;
    private boolean leida = false;
    private LocalDateTime fecha;

    public NotificacionEntity() {}

    // Getters y setters
    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }
    public String getTipo()               { return tipo; }
    public void setTipo(String tipo)      { this.tipo = tipo; }
    public String getTitulo()             { return titulo; }
    public void setTitulo(String titulo)  { this.titulo = titulo; }
    public String getMensaje()            { return mensaje; }
    public void setMensaje(String msg)    { this.mensaje = msg; }
    public String getNivel()              { return nivel; }
    public void setNivel(String nivel)    { this.nivel = nivel; }
    public boolean isLeida()              { return leida; }
    public void setLeida(boolean leida)   { this.leida = leida; }
    public LocalDateTime getFecha()       { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
}
