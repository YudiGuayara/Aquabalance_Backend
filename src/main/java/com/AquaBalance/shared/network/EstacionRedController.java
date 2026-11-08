package com.AquaBalance.network;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionRedController {

    private final EstacionRedService service;

    public EstacionRedController(EstacionRedService service) {
        this.service = service;
    }

    @GetMapping
    public List<EstacionRedResponse> listar() {
        return service.listar();
    }

    /** Endpoint al que cada estación (sensor) envía su telemetría. */
    @PostMapping("/{id}/telemetria")
    public EstacionRedResponse telemetria(@PathVariable Long id,
                                          @RequestBody TelemetriaRequest datos) {
        return service.registrarTelemetria(id, datos);
    }


    /** Registra un paquete que no llegó (para calcular el porcentaje de pérdida). */
    @PostMapping("/{id}/perdida")
    public EstacionRedResponse perdida(@PathVariable Long id) {
        return service.registrarPerdida(id);
    }
}