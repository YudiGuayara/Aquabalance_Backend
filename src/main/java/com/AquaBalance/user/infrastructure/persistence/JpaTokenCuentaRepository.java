package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.domain.TipoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JpaTokenCuentaRepository extends JpaRepository<TokenCuentaEntity, Long> {

    Optional<TokenCuentaEntity> findByTokenHashAndTipo(String tokenHash, TipoToken tipo);

    @Transactional
    void deleteByUsuarioIdAndTipo(Long usuarioId, TipoToken tipo);
}
