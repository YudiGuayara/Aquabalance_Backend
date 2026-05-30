package com.AquaBalance.user.application;

import com.AquaBalance.shared.exception.BusinessException;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.GestionarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements
        RegistrarUsuarioUseCase,
        BuscarUsuarioUseCase,
        GestionarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    // ── RegistrarUsuarioUseCase ───────────────────────────────

    @Override
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new BusinessException(
                    "El correo electrónico ya está registrado en el sistema.");
        }

        // ✅ asignar fecha si no viene
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }

        // ✅ encriptar password si no está encriptado
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    // ── BuscarUsuarioUseCase ──────────────────────────────────

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id));
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con el correo: " + email));
    }

    @Override
    public List<Usuario> listarActivos() {
        return usuarioRepository.findAllActivos();
    }

    // ── GestionarUsuarioUseCase ───────────────────────────────

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario actualizar(Long id, Usuario nuevo) {
        Usuario existente = buscarPorId(id);
        existente.setNombre(nuevo.getNombre());
        existente.setEmail(nuevo.getEmail());
        existente.setRol(nuevo.getRol());

        // ✅ solo encriptar si viene nueva password
        if (nuevo.getPassword() != null && !nuevo.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(nuevo.getPassword()));
        }

        if (nuevo.isActivo()) existente.activar();
        else                  existente.desactivar();

        return usuarioRepository.save(existente);
    }

    @Override
    public void eliminarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);  // ← borrado físico
    }

    @Override
    public void desactivarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.desactivar();
        usuarioRepository.save(usuario);
    }

    @Override
    public void activarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.activar();
        usuarioRepository.save(usuario);
    }
}