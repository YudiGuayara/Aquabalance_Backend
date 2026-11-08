package com.AquaBalance.reports.infrastructure.web;

import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase;
import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.reports.infrastructure.web.dto.InformeRequest;
import com.AquaBalance.reports.infrastructure.web.dto.InformeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final GestionarInformeUseCase useCase;

    public InformeController(GestionarInformeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<InformeResponse> crear(@RequestBody InformeRequest request) {
        Informe guardado = useCase.crearInforme(InformeMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InformeMapper.fromInforme(guardado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformeResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(InformeMapper.fromDetalle(useCase.obtenerInforme(id)));
    }

    @GetMapping
    public ResponseEntity<List<InformeResponse>> listar() {
        List<InformeResponse> lista = useCase.listarInformes().stream()
                .map(InformeMapper::fromResumen)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InformeResponse> actualizar(
            @PathVariable Long id, @RequestBody InformeRequest request) {
        Informe actualizado = useCase.actualizarInforme(id, InformeMapper.toDomain(request));
        return ResponseEntity.ok(InformeMapper.fromInforme(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminarInforme(id);
        return ResponseEntity.noContent().build();
    }
}