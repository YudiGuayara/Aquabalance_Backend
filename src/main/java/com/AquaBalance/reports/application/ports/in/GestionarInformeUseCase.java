package com.AquaBalance.reports.application.ports.in;

import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.reports.domain.Estadisticas;
import java.util.List;

public interface GestionarInformeUseCase {
    Informe crearInforme(Informe informe);
    Informe actualizarInforme(Long id, Informe informe);
    void eliminarInforme(Long id);
    List<InformeResumen> listarInformes();
    InformeDetalle obtenerInforme(Long id);

    // ── Clases de resultado ──────────────────────────────────

    class InformeResumen {
        private final Informe informe;
        private final String nombreRecurso;
        private final String nombreContaminante;

        public InformeResumen(Informe informe, String nombreRecurso, String nombreContaminante) {
            this.informe            = informe;
            this.nombreRecurso      = nombreRecurso;
            this.nombreContaminante = nombreContaminante;
        }
        public Informe getInforme() { return informe; }
        public String getNombreRecurso() { return nombreRecurso; }
        public String getNombreContaminante() { return nombreContaminante; }
    }

    class InformeDetalle {
        private final Informe informe;
        private final String nombreRecurso;
        private final String nombreContaminante;
        private final Estadisticas estadisticas;

        public InformeDetalle(Informe informe, String nombreRecurso,
                              String nombreContaminante, Estadisticas estadisticas) {
            this.informe            = informe;
            this.nombreRecurso      = nombreRecurso;
            this.nombreContaminante = nombreContaminante;
            this.estadisticas       = estadisticas;
        }
        public Informe getInforme() { return informe; }
        public String getNombreRecurso() { return nombreRecurso; }
        public String getNombreContaminante() { return nombreContaminante; }
        public Estadisticas getEstadisticas() { return estadisticas; }
    }
}