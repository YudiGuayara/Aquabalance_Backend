package com.AquaBalance.reports.infrastructure.web;

import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase;
import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase.InformeDetalle;
import com.AquaBalance.reports.application.ports.in.GestionarInformeUseCase.InformeResumen;
import com.AquaBalance.reports.domain.Estadisticas;
import com.AquaBalance.reports.domain.Informe;
import com.AquaBalance.reports.infrastructure.web.dto.InformeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InformeController - Pruebas Unitarias")
class InformeControllerTest {

    @Mock  private GestionarInformeUseCase useCase;
    @InjectMocks private InformeController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Informe informe;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        informe = new Informe();
        informe.setId(1L);
        informe.setTitulo("Informe Mensual");
        informe.setDescripcion("Análisis de mayo");
        informe.setFechaGeneracion(LocalDateTime.of(2025, 5, 1, 10, 0));
        informe.setFechaInicio(LocalDateTime.of(2025, 4, 1, 0, 0));
        informe.setFechaFin(LocalDateTime.of(2025, 4, 30, 23, 59));
        informe.setRecursoId(1L);
        informe.setContaminanteId(1L);
    }

    private InformeRequest buildRequest() {
        InformeRequest req = new InformeRequest();
        req.setTitulo("Informe Mensual");
        req.setDescripcion("Análisis de mayo");
        req.setFechaInicio(LocalDateTime.of(2025, 4, 1, 0, 0));
        req.setFechaFin(LocalDateTime.of(2025, 4, 30, 23, 59));
        req.setRecursoId(1L);
        req.setContaminanteId(1L);
        return req;
    }

    @Nested
    @DisplayName("POST /api/informes")
    class Crear {

        @Test
        @DisplayName("Debe retornar 201 con el informe creado")
        void debeRetornar201() throws Exception {
            when(useCase.crearInforme(any())).thenReturn(informe);

            mockMvc.perform(post("/api/informes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.titulo").value("Informe Mensual"));
        }
    }

    @Nested
    @DisplayName("GET /api/informes/{id}")
    class Obtener {

        @Test
        @DisplayName("Debe retornar informe con detalle")
        void debeRetornarConDetalle() throws Exception {
            Estadisticas stats = new Estadisticas();
            stats.setTotalMediciones(10L);
            InformeDetalle detalle = new InformeDetalle(informe, "Río Bogotá", "Mercurio", stats);
            when(useCase.obtenerInforme(1L)).thenReturn(detalle);

            mockMvc.perform(get("/api/informes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.titulo").value("Informe Mensual"))
                    .andExpect(jsonPath("$.nombreRecurso").value("Río Bogotá"))
                    .andExpect(jsonPath("$.estadisticas.totalMediciones").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/informes")
    class Listar {

        @Test
        @DisplayName("Debe retornar lista de informes")
        void debeRetornarLista() throws Exception {
            Informe i2 = new Informe();
            i2.setId(2L);
            i2.setTitulo("Informe Anual");
            InformeResumen r1 = new InformeResumen(informe, "Río Bogotá", "Mercurio");
            InformeResumen r2 = new InformeResumen(i2, "Lago Tota", "Arsénico");
            when(useCase.listarInformes()).thenReturn(List.of(r1, r2));

            mockMvc.perform(get("/api/informes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].titulo").value("Informe Mensual"))
                    .andExpect(jsonPath("$[1].titulo").value("Informe Anual"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay informes")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarInformes()).thenReturn(List.of());

            mockMvc.perform(get("/api/informes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/informes/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar informe actualizado")
        void debeActualizar() throws Exception {
            Informe actualizado = new Informe();
            actualizado.setId(1L);
            actualizado.setTitulo("Informe Actualizado");
            when(useCase.actualizarInforme(eq(1L), any())).thenReturn(actualizado);

            mockMvc.perform(put("/api/informes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titulo").value("Informe Actualizado"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/informes/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminarInforme(1L);

            mockMvc.perform(delete("/api/informes/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminarInforme(1L);
        }
    }
}
