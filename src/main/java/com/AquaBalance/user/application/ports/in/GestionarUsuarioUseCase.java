package com.AquaBalance.user.application.ports.in;

import com.AquaBalance.user.domain.Usuario;
import java.util.List;

public interface GestionarUsuarioUseCase {
    void          desactivarUsuario(Long id);
    void          activarUsuario(Long id);
    List<Usuario> listarTodos();
    Usuario       actualizar(Long id, Usuario usuario);
}