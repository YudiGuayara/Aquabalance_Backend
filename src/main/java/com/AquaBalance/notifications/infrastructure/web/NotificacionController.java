package com.AquaBalance.notifications.infrastructure.web;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.notifications.domain.Notificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final GestionarNotificacionUseCase useCase;

    public NotificacionController(GestionarNotificacionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(useCase.obtenerHistorial());
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        return ResponseEntity.ok(
                Map.of("cantidad", useCase.contarNoLeidas())
        );
    }

    @PutMapping("/marcar-leidas")
    public ResponseEntity<Void> marcarTodasLeidas() {
        useCase.marcarTodasLeidas();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        useCase.marcarLeida(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> enviarPrueba(@RequestBody Notificacion notificacion) {
        useCase.notificar(notificacion);
        return ResponseEntity.ok().build();
    }
}