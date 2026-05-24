package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.RecursoRepository;
import com.AquaBalance.reports.application.ports.out.RecursoConsultaPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class RecursoConsultaAdapter implements RecursoConsultaPort {

    private final RecursoRepository recursoRepository;

    public RecursoConsultaAdapter(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Override
    public Optional<String> buscarNombrePorId(Long id) {
        return recursoRepository.buscarPorId(id).map(r -> r.getNombre());
    }
}