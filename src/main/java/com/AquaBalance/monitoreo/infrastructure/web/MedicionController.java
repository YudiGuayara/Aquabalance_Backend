package com.AquaBalance.monitoreo.infrastructure.web;

import com.AquaBalance.monitoreo.application.MedicionDTO;
import com.AquaBalance.monitoreo.application.ports.in.RegistrarMedicionUseCase;
import com.AquaBalance.monitoreo.infrastructure.web.dto.MedicionRequest;
import com.AquaBalance.monitoreo.infrastructure.web.dto.MedicionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoreo/mediciones")
public class MedicionController {

    private final RegistrarMedicionUseCase useCase;

    public MedicionController(RegistrarMedicionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<MedicionResponse> registrar(@RequestBody MedicionRequest request) {
        MedicionDTO dto = useCase.registrar(toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicionResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<MedicionResponse>> listarTodos() {
        List<MedicionResponse> lista = useCase.listarTodos()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/recurso/{idRecurso}")
    public ResponseEntity<List<MedicionResponse>> listarPorRecurso(@PathVariable Long idRecurso) {
        List<MedicionResponse> lista = useCase.listarPorRecurso(idRecurso)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicionResponse> actualizar(
            @PathVariable Long id,
            @RequestBody MedicionRequest request) {

        MedicionDTO dto = useCase.actualizar(id, toDTO(request));
        return ResponseEntity.ok(toResponse(dto));
    }

    private MedicionDTO toDTO(MedicionRequest r) {
        return new MedicionDTO(null, r.getPh(), r.getTemperatura(), null, r.getIdUsuario(), r.getIdRecurso(), r.getIdContaminante());
    }

    private MedicionResponse toResponse(MedicionDTO dto) {
        return new MedicionResponse(dto.getId(), dto.getPh(), dto.getTemperatura(), dto.getFecha(), dto.getIdUsuario(), dto.getIdRecurso(), dto.getIdContaminante());
    }
}