package com.AquaBalance.user.application;

import com.AquaBalance.notifications.infrastructure.email.EmailAdapter;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.application.ports.out.TokenPort;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.infrastructure.security.CustomUserDetails;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final BuscarUsuarioUseCase    buscarUsuarioUseCase;
    private final TokenPort               tokenPort;
    private final PasswordEncoder         passwordEncoder;
    private final AuthenticationManager   authenticationManager;
    private final EmailAdapter            emailAdapter;

    public AuthService(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                       BuscarUsuarioUseCase buscarUsuarioUseCase,
                       TokenPort tokenPort,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       EmailAdapter emailAdapter) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.buscarUsuarioUseCase    = buscarUsuarioUseCase;
        this.tokenPort               = tokenPort;
        this.passwordEncoder         = passwordEncoder;
        this.authenticationManager   = authenticationManager;
        this.emailAdapter            = emailAdapter;
    }

    public AuthResponse registrar(RegisterRequest request) {

        Usuario usuario = new Usuario(
                null,
                request.getNombre(),
                request.getEmail(),
                request.getPassword(), // <- SIN encode
                Rol.valueOf(request.getRol())
        );

        Usuario guardado = registrarUsuarioUseCase.registrar(usuario);

        // Enviar correo de bienvenida al email del usuario recién registrado
        emailAdapter.enviarBienvenida(guardado.getEmail(), guardado.getNombre());

        CustomUserDetails userDetails = new CustomUserDetails(guardado);
        String token = tokenPort.generateToken(userDetails);

        return new AuthResponse(
                guardado.getId(),
                token,
                guardado.getEmail(),
                guardado.getNombre(),
                guardado.getRol().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = buscarUsuarioUseCase.buscarPorEmail(request.getEmail());
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String token = tokenPort.generateToken(userDetails);

        return new AuthResponse(
                usuario.getId(),
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
        );
    }
}