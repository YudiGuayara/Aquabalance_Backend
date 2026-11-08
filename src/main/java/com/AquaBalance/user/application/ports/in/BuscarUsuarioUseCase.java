package com.AquaBalance.user.application.ports.in;

import com.AquaBalance.user.domain.Usuario;
import java.util.List;

public interface BuscarUsuarioUseCase {
    Usuario buscarPorId(Long id);
    Usuario buscarPorEmail(String email);
    List<Usuario> listarActivos();
}