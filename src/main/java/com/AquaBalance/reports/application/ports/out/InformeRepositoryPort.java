package com.AquaBalance.reports.application.ports.out;

import com.AquaBalance.reports.domain.Informe;
import java.util.List;
import java.util.Optional;

public interface InformeRepositoryPort {
    Informe save(Informe informe);
    Optional<Informe> findById(Long id);
    List<Informe> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}