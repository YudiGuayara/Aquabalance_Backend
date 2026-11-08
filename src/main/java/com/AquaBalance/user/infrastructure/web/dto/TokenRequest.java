package com.AquaBalance.user.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
