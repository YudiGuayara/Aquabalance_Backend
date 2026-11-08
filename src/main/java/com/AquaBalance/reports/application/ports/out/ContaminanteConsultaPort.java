package com.AquaBalance.reports.application.ports.out;

import java.util.Optional;

public interface ContaminanteConsultaPort {
    Optional<String> buscarNombrePorId(Long id);
}