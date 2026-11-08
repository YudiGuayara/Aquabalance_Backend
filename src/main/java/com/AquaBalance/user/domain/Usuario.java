package com.AquaBalance.user.domain;

import java.time.LocalDateTime;

public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private String password;
    private Rol rol;
    private boolean activo;
    private boolean emailVerificado;
    private LocalDateTime fechaCreacion;

    public Usuario(Long id, String nombre, String email, String password, Rol rol) {
        this.id            = id;
        this.nombre        = nombre;
        this.email         = email;
        this.password      = password;
        this.rol           = rol;
        this.activo        = true;
        this.emailVerificado = true; // las cuentas nuevas por registro publico se marcan false en AuthService
        this.fechaCreacion = LocalDateTime.now();
    }

    public Usuario() {}


    public void desactivar() { this.activo = false; }
    public void activar()    { this.activo = true;  }

    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }

    public String getNombre()                              { return nombre; }
    public void setNombre(String nombre)                   { this.nombre = nombre; }

    public String getEmail()                               { return email; }
    public void setEmail(String email)                     { this.email = email; }

    public String getPassword()                            { return password; }
    public void setPassword(String password)               { this.password = password; }

    public Rol getRol()                                    { return rol; }
    public void setRol(Rol rol)                            { this.rol = rol; }

    public boolean isActivo()                              { return activo; }
    public void setActivo(boolean activo)                  { this.activo = activo; } // ← NUEVO

    public boolean isEmailVerificado()                     { return emailVerificado; }
    public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }

    public LocalDateTime getFechaCreacion()                { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}