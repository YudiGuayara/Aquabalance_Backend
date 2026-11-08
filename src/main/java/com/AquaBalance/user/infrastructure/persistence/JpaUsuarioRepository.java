package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    List<UsuarioEntity> findByActivoTrue();

    boolean existsByRol(Rol rol);
}