package com.AquaBalance.monitoreo.infrastructure.web;

import com.AquaBalance.monitoreo.application.ContaminanteDTO;
import com.AquaBalance.monitoreo.application.ports.in.GestionarContaminanteUseCase;
import com.AquaBalance.monitoreo.infrastructure.web.dto.ContaminanteRequest;
import com.AquaBalance.monitoreo.infrastructure.web.dto.ContaminanteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoreo/contaminantes")
public class ContaminanteController {

    private final GestionarContaminanteUseCase useCase;

    public ContaminanteController(GestionarContaminanteUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ContaminanteResponse> crear(@RequestBody ContaminanteRequest request) {
        ContaminanteDTO dto = useCase.crear(toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaminanteResponse> actualizar(@PathVariable Long id, @RequestBody ContaminanteRequest request) {
        ContaminanteDTO dto = useCase.actualizar(id, toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaminanteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<ContaminanteResponse>> listarTodos() {
        List<ContaminanteResponse> lista = useCase.listarTodos()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ContaminanteDTO toDTO(ContaminanteRequest r) {
        return new ContaminanteDTO(null, r.getNombre(), r.getCarga(), r.getNivel(), r.getFuenteOrigen());
    }

    private ContaminanteResponse toResponse(ContaminanteDTO dto) {
        return new ContaminanteResponse(dto.getId(), dto.getNombre(), dto.getCarga(), dto.getNivel(), dto.getFuenteOrigen());
    }
}