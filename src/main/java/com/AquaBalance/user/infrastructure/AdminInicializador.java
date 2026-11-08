package com.AquaBalance.user.infrastructure;

import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador unico al arrancar, SOLO si todavia no existe ninguno.
 * Las credenciales vienen de variables de entorno (ADMIN_EMAIL y ADMIN_PASSWORD),
 * nunca del codigo ni de un formulario publico.
 */
@Component
public class AdminInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UsuarioRepository       usuarioRepository;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Value("${admin.email:}")
    private String email;

    @Value("${admin.password:}")
    private String password;

    @Value("${admin.nombre:Administrador}")
    private String nombre;

    public AdminInicializador(UsuarioRepository usuarioRepository,
                              RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.usuarioRepository       = usuarioRepository;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByRol(Rol.Administrador)) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("No existe ningún administrador y faltan ADMIN_EMAIL / ADMIN_PASSWORD. "
                    + "Define esas variables de entorno y reinicia para crearlo.");
            return;
        }
        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD debe tener al menos 8 caracteres. No se creó el administrador.");
            return;
        }

        Usuario admin = new Usuario(null, nombre, email.trim().toLowerCase(), password, Rol.Administrador);
        admin.setEmailVerificado(true);
        registrarUsuarioUseCase.registrar(admin);
        log.info("Administrador inicial creado: {}", admin.getEmail());
    }
}
