package com.AquaBalance.monitoring.infrastructure.web;

import com.AquaBalance.monitoring.application.ports.in.GestionarRecursoUseCase;
import com.AquaBalance.monitoring.infrastructure.web.dto.RecursoRequest;
import com.AquaBalance.monitoring.infrastructure.web.dto.RecursoResponse;
import com.AquaBalance.monitoring.application.RecursoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoreo/recursos")
public class RecursoController {

    private final GestionarRecursoUseCase useCase;

    public RecursoController(GestionarRecursoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<RecursoResponse> crear(@RequestBody RecursoRequest request) {
        RecursoDTO dto = useCase.crear(toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecursoResponse> actualizar(@PathVariable Long id, @RequestBody RecursoRequest request) {
        RecursoDTO dto = useCase.actualizar(id, toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecursoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<RecursoResponse>> listarTodos() {
        List<RecursoResponse> lista = useCase.listarTodos()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private RecursoDTO toDTO(RecursoRequest r) {
        return new RecursoDTO(null, r.getNombre(), r.getTipo(), r.getUbicacion(), r.getLatitud(), r.getLongitud());
    }

    private RecursoResponse toResponse(RecursoDTO dto) {
        return new RecursoResponse(dto.getId(), dto.getNombre(), dto.getTipo(), dto.getUbicacion(), dto.getLatitud(), dto.getLongitud());
    }
}