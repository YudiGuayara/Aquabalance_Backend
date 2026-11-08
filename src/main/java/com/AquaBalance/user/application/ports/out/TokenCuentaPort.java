package com.AquaBalance.user.application.ports.out;

import com.AquaBalance.user.domain.TipoToken;
import com.AquaBalance.user.domain.TokenCuenta;

import java.util.Optional;

public interface TokenCuentaPort {
    TokenCuenta save(TokenCuenta token);
    Optional<TokenCuenta> findByHashAndTipo(String tokenHash, TipoToken tipo);
    void deleteByUsuarioIdAndTipo(Long usuarioId, TipoToken tipo);
}
