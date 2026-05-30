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

    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {
        return ResponseEntity.ok(
                gestionarUseCase.listarTodos()
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarUseCase.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UserDTO> crear(@RequestBody UserDTO dto) {
        Usuario guardado = registrarUseCase.registrar(toDomain(dto));
        return ResponseEntity.ok(toDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> actualizar(@PathVariable Long id,
                                              @RequestBody UserDTO dto) {
        Usuario actualizado = gestionarUseCase.actualizar(id, toDomain(dto));
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        gestionarUseCase.activarUsuario(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        gestionarUseCase.desactivarUsuario(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gestionarUseCase.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    private UserDTO toDTO(Usuario u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setRol(u.getRol() != null ? u.getRol().name() : null);
        dto.setActivo(u.isActivo());
        dto.setFechaCreacion(u.getFechaCreacion());
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