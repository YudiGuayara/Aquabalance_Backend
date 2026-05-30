package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JpaUsuarioRepository jpaRepository;

    public UsuarioRepositoryImpl(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity      = toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Usuario> findAllActivos() {
        return jpaRepository.findByActivoTrue()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Usuario usuario) {
        jpaRepository.deleteById(usuario.getId()); // ← borrado físico
    }

    // ── Mappers ───────────────────────────────────────────────

    private UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(
                domain.getId(),
                domain.getNombre(),
                domain.getEmail(),
                domain.getPassword(),
                domain.getRol(),
                domain.isActivo(),
                domain.getFechaCreacion()
        );
    }

    private Usuario toDomain(UsuarioEntity entity) {
        Usuario usuario = new Usuario(
                entity.getId(),
                entity.getNombre(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRol()
        );
        if (!entity.isActivo()) usuario.desactivar();
        usuario.setFechaCreacion(entity.getFechaCreacion());
        return usuario;
    }
}