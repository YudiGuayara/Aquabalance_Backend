package com.AquaBalance.reports.application.ports.out;

import java.util.Optional;

public interface RecursoConsultaPort {
    Optional<String> buscarNombrePorId(Long id);
}