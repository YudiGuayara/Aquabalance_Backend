package com.AquaBalance.events.infrastructure.web;

import com.AquaBalance.events.application.AlertaDTO;
import com.AquaBalance.events.application.ports.in.GestionarAlertaUseCase;
import com.AquaBalance.events.domain.NivelAlerta;
import com.AquaBalance.events.infrastructure.web.dto.AlertaRequest;
import com.AquaBalance.events.infrastructure.web.dto.AlertaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final GestionarAlertaUseCase useCase;

    public AlertaController(GestionarAlertaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<AlertaResponse> crear(@RequestBody AlertaRequest request) {
        AlertaDTO dto = useCase.crear(toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listarTodos() {
        List<AlertaResponse> lista = useCase.listarTodos()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<AlertaResponse>> listarPorNivel(@PathVariable NivelAlerta nivel) {
        List<AlertaResponse> lista = useCase.listarPorNivel(nivel)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/evento/{idEvento}")
    public ResponseEntity<List<AlertaResponse>> listarPorEvento(@PathVariable Long idEvento) {
        List<AlertaResponse> lista = useCase.listarPorEvento(idEvento)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponse> actualizar(@PathVariable Long id, @RequestBody AlertaRequest request) {
        AlertaDTO dto = useCase.actualizar(id, toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private AlertaDTO toDTO(AlertaRequest r) {
        return new AlertaDTO(null, null, r.getNivel(), r.getMensaje(), r.getIdUsuario(), r.getIdEvento());
    }

    private AlertaResponse toResponse(AlertaDTO dto) {
        return new AlertaResponse(dto.getId(), dto.getFecha(), dto.getNivel(), dto.getMensaje(), dto.getIdUsuario(), dto.getIdEvento());
    }
}