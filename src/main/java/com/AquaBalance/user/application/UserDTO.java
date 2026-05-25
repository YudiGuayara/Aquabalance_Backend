package com.AquaBalance.user.application;

import java.time.LocalDateTime;

public class UserDTO {

    private Long          id;
    private String        nombre;
    private String        email;
    private String        password;
    private String        rol;
    private boolean       activo;
    private LocalDateTime fechaCreacion;

    public UserDTO() {}

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }

    public String getNombre()                     { return nombre; }
    public void setNombre(String nombre)          { this.nombre = nombre; }

    public String getEmail()                      { return email; }
    public void setEmail(String email)            { this.email = email; }

    public String getPassword()                   { return password; }
    public void setPassword(String password)      { this.password = password; }

    public String getRol()                        { return rol; }
    public void setRol(String rol)                { this.rol = rol; }

    public boolean isActivo()                     { return activo; }
    public void setActivo(boolean activo)         { this.activo = activo; }

    public LocalDateTime getFechaCreacion()       { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
}