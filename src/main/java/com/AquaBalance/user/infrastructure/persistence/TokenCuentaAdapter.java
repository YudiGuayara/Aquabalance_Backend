package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.application.ports.out.TokenCuentaPort;
import com.AquaBalance.user.domain.TipoToken;
import com.AquaBalance.user.domain.TokenCuenta;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenCuentaAdapter implements TokenCuentaPort {

    private final JpaTokenCuentaRepository jpa;

    public TokenCuentaAdapter(JpaTokenCuentaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TokenCuenta save(TokenCuenta token) {
        return toDomain(jpa.save(toEntity(token)));
    }

    @Override
    public Optional<TokenCuenta> findByHashAndTipo(String tokenHash, TipoToken tipo) {
        return jpa.findByTokenHashAndTipo(tokenHash, tipo).map(this::toDomain);
    }

    @Override
    public void deleteByUsuarioIdAndTipo(Long usuarioId, TipoToken tipo) {
        jpa.deleteByUsuarioIdAndTipo(usuarioId, tipo);
    }

    private TokenCuentaEntity toEntity(TokenCuenta d) {
        TokenCuentaEntity e = new TokenCuentaEntity();
        e.setId(d.getId());
        e.setUsuarioId(d.getUsuarioId());
        e.setTokenHash(d.getTokenHash());
        e.setTipo(d.getTipo());
        e.setExpiraEn(d.getExpiraEn());
        e.setUsado(d.isUsado());
        return e;
    }

    private TokenCuenta toDomain(TokenCuentaEntity e) {
        TokenCuenta d = new TokenCuenta(e.getUsuarioId(), e.getTokenHash(), e.getTipo(), e.getExpiraEn());
        d.setId(e.getId());
        d.setUsado(e.isUsado());
        return d;
    }
}
