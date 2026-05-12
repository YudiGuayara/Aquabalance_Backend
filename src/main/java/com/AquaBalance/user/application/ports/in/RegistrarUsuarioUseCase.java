package com.AquaBalance.user.application.ports.in;

import com.AquaBalance.user.domain.Usuario;

public interface RegistrarUsuarioUseCase {
    Usuario registrar(Usuario usuario);
}