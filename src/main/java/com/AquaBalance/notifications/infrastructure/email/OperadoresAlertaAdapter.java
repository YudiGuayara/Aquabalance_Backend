package com.AquaBalance.notifications.infrastructure.email;

import com.AquaBalance.notifications.application.ports.out.DestinatariosAlertaPort;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class OperadoresAlertaAdapter implements DestinatariosAlertaPort {

    // Para que las alertas también lleguen a los administradores, agregar Rol.Administrador aquí.
    private static final Set<Rol> ROLES_DESTINO = Set.of(Rol.Operador);

    private final UsuarioRepository usuarioRepository;

    public OperadoresAlertaAdapter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<String> emailsDeOperadores() {
        return usuarioRepository.findAllActivos().stream()
                .filter(u -> ROLES_DESTINO.contains(u.getRol()))
                .filter(Usuario::isEmailVerificado)
                .map(Usuario::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
