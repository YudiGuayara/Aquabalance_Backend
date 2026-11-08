package com.AquaBalance.user.infrastructure.web;

import com.AquaBalance.user.application.AuthService;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.EmailRequest;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.MensajeResponse;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
import com.AquaBalance.user.infrastructure.web.dto.ResetPasswordRequest;
import com.AquaBalance.user.infrastructure.web.dto.TokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<MensajeResponse> registro(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(new MensajeResponse(authService.registrar(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verificar-email")
    public ResponseEntity<MensajeResponse> verificarEmail(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(new MensajeResponse(authService.verificarEmail(request.getToken())));
    }

    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<MensajeResponse> reenviarVerificacion(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(new MensajeResponse(authService.reenviarVerificacion(request.getEmail())));
    }

    @PostMapping("/olvide-password")
    public ResponseEntity<MensajeResponse> olvidePassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(new MensajeResponse(authService.olvidePassword(request.getEmail())));
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<MensajeResponse> restablecerPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(new MensajeResponse(
                authService.restablecerPassword(request.getToken(), request.getNuevaPassword())));
    }
}
