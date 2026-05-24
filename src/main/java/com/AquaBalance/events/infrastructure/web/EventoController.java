package com.AquaBalance.events.infrastructure.web;


import com.AquaBalance.events.application.EventoDTO;
import com.AquaBalance.events.application.ports.in.GestionarEventoUseCase;
import com.AquaBalance.events.infrastructure.web.dto.EventoRequest;
import com.AquaBalance.events.infrastructure.web.dto.EventoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final GestionarEventoUseCase useCase;

    public EventoController(GestionarEventoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<EventoResponse> crear(@RequestBody EventoRequest request) {
        EventoDTO dto = useCase.crear(toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        List<EventoResponse> lista = useCase.listarTodos()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @GetMapping("/recurso/{idRecurso}")
    public ResponseEntity<List<EventoResponse>> listarPorRecurso(@PathVariable Long idRecurso) {
        List<EventoResponse> lista = useCase.listarPorRecurso(idRecurso)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> actualizar(@PathVariable Long id, @RequestBody EventoRequest request) {
        EventoDTO dto = useCase.actualizar(id, toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EventoDTO toDTO(EventoRequest r) {
        return new EventoDTO(null, r.getDescripcion(), r.getMagnitud(), null, r.getIdContaminante(), r.getIdRecurso());
    }

    private EventoResponse toResponse(EventoDTO dto) {
        return new EventoResponse(dto.getId(), dto.getDescripcion(), dto.getMagnitud(), dto.getFecha(), dto.getIdContaminante(), dto.getIdRecurso());
    }
}
