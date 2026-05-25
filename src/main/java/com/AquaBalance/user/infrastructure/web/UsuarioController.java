package com.AquaBalance.user.infrastructure.web;

import com.AquaBalance.user.application.UserDTO;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.GestionarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final BuscarUsuarioUseCase    buscarUseCase;
    private final GestionarUsuarioUseCase gestionarUseCase;
    private final RegistrarUsuarioUseCase registrarUseCase;

    public UsuarioController(BuscarUsuarioUseCase buscarUseCase,
                             GestionarUsuarioUseCase gestionarUseCase,
                             RegistrarUsuarioUseCase registrarUseCase) {
        this.buscarUseCase    = buscarUseCase;
        this.gestionarUseCase = gestionarUseCase;
        this.registrarUseCase = registrarUseCase;
    }

    // ── GET /api/usuarios ─────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {
        return ResponseEntity.ok(
                gestionarUseCase.listarTodos()
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );
    }

    // ── GET /api/usuarios/{id} ────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarUseCase.buscarPorId(id)));
    }

    // ── POST /api/usuarios ────────────────────────────────────

    @PostMapping
    public ResponseEntity<UserDTO> crear(@RequestBody UserDTO dto) {
        Usuario guardado = registrarUseCase.registrar(toDomain(dto));
        return ResponseEntity.ok(toDTO(guardado));
    }

    // ── PUT /api/usuarios/{id} ────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> actualizar(@PathVariable Long id,
                                              @RequestBody UserDTO dto) {
        Usuario actualizado = gestionarUseCase.actualizar(id, toDomain(dto));
        return ResponseEntity.ok(toDTO(actualizado));
    }

    // ── PATCH /api/usuarios/{id}/toggle-activo ────────────────

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id) {
        Usuario u = buscarUseCase.buscarPorId(id);
        if (u.isActivo()) gestionarUseCase.desactivarUsuario(id);
        else              gestionarUseCase.activarUsuario(id);
        return ResponseEntity.ok().build();
    }

    // ── DELETE /api/usuarios/{id} ─────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gestionarUseCase.desactivarUsuario(id);
        return ResponseEntity.ok().build();
    }

    // ── Mappers ───────────────────────────────────────────────

    private UserDTO toDTO(Usuario u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setRol(u.getRol() != null ? u.getRol().name() : null);
        dto.setActivo(u.isActivo());
        dto.setFechaCreacion(u.getFechaCreacion());
        // password nunca se devuelve al frontend
        return dto;
    }

    private Usuario toDomain(UserDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setRol(dto.getRol() != null ? Rol.valueOf(dto.getRol()) : Rol.Operador);
        u.setActivo(dto.isActivo());
        return u;
    }
}