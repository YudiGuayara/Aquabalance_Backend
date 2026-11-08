package com.AquaBalance.user.domain;

import java.time.LocalDateTime;

/**
 * Token de un solo uso para verificar el correo o restablecer la contraseña.
 * En la BD solo se guarda el HASH del token, nunca el token en claro.
 */
public class TokenCuenta {

    private Long id;
    private Long usuarioId;
    private String tokenHash;
    private TipoToken tipo;
    private LocalDateTime expiraEn;
    private boolean usado;

    public TokenCuenta() {}

    public TokenCuenta(Long usuarioId, String tokenHash, TipoToken tipo, LocalDateTime expiraEn) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.tipo      = tipo;
        this.expiraEn  = expiraEn;
        this.usado     = false;
    }

    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(expiraEn);
    }

    public void marcarUsado() { this.usado = true; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public TipoToken getTipo() { return tipo; }
    public void setTipo(TipoToken tipo) { this.tipo = tipo; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }
    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
