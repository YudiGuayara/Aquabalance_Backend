package com.AquaBalance.reports.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.ContaminanteRepository;
import com.AquaBalance.reports.application.ports.out.ContaminanteConsultaPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ContaminanteConsultaAdapter implements ContaminanteConsultaPort {

    private final ContaminanteRepository contaminanteRepository;

    public ContaminanteConsultaAdapter(ContaminanteRepository contaminanteRepository) {
        this.contaminanteRepository = contaminanteRepository;
    }

    @Override
    public Optional<String> buscarNombrePorId(Long id) {
        return contaminanteRepository.buscarPorId(id).map(c -> c.getNombre());
    }
}