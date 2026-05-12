package com.AquaBalance.user.infrastructure.web;

import com.AquaBalance.user.application.AuthService;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
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
    public ResponseEntity<AuthResponse> registro(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
