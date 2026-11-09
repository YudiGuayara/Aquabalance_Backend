package com.AquaBalance.shared.network;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint ligero para medir la latencia y el estado de la conexión
 * entre el cliente y el servidor. Es público (no requiere token).
 */
@RestController
@RequestMapping("/api/network")
public class NetworkController package com.AquaBalance.shared.network;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint ligero para medir la latencia y el estado de la conexión
 * entre el cliente y el servidor. Es público (no requiere token).
 */
@RestController
@RequestMapping("/api/network")
public class NetworkController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(HttpServletRequest request) {
        Map<String, Object> body = Map.of(
                "status", "ok",
                "timestamp", System.currentTimeMillis(),
                "clientIp", obtenerIpCliente(request),
                "build", "2026-11-09-b"
        );

        // Sin caché: cada medición debe viajar realmente por la red
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    /** Detrás de un proxy (Render, nginx) la IP real llega en X-Forwarded-For. */
    private String obtenerIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}