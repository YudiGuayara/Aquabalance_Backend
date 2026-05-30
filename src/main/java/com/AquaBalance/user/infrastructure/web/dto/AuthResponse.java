package com.AquaBalance.user.infrastructure.web.dto;

public class AuthResponse {

    private Long id;
    private String token;
    private String email;
    private String nombre;
    private String rol;

    public AuthResponse() {}

    public AuthResponse(Long id, String token, String email,
                        String nombre, String rol) {
        this.id     = id;
        this.token  = token;
        this.email  = email;
        this.nombre = nombre;
        this.rol    = rol;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}