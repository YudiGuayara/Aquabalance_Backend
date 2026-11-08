package com.AquaBalance.monitoring.infrastructure.web;

import com.AquaBalance.monitoring.application.RecursoDTO;
import com.AquaBalance.monitoring.application.ports.in.GestionarRecursoUseCase;
import com.AquaBalance.monitoring.domain.TipoRecurso;
import com.AquaBalance.monitoring.infrastructure.web.dto.RecursoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecursoController - Pruebas Unitarias")
class RecursoControllerTest {

    @Mock  private GestionarRecursoUseCase useCase;
    @InjectMocks private RecursoController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RecursoDTO recursoDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        recursoDTO = new RecursoDTO(1L, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", 4.61, -74.08);
    }

    private RecursoRequest buildRequest() {
        RecursoRequest req = new RecursoRequest();
        req.setNombre("Río Bogotá");
        req.setTipo(TipoRecurso.Rio);
        req.setUbicacion("Cundinamarca");
        req.setLatitud(4.61);
        req.setLongitud(-74.08);
        return req;
    }

    @Nested
    @DisplayName("POST /api/monitoreo/recursos")
    class Crear {

        @Test
        @DisplayName("Debe retornar 200 con el recurso creado")
        void debeRetornar200() throws Exception {
            when(useCase.crear(any())).thenReturn(recursoDTO);

            mockMvc.perform(post("/api/monitoreo/recursos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Río Bogotá"))
                    .andExpect(jsonPath("$.tipo").value("Rio"));
        }
    }

    @Nested
    @DisplayName("PUT /api/monitoreo/recursos/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar recurso actualizado")
        void debeActualizar() throws Exception {
            RecursoDTO actualizado = new RecursoDTO(1L, "Lago Tota", TipoRecurso.Lago, "Boyacá", 5.56, -72.92);
            when(useCase.actualizar(eq(1L), any())).thenReturn(actualizado);

            mockMvc.perform(put("/api/monitoreo/recursos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Lago Tota"))
                    .andExpect(jsonPath("$.tipo").value("Lago"));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/recursos/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar recurso por ID")
        void debeRetornarPorId() throws Exception {
            when(useCase.buscarPorId(1L)).thenReturn(recursoDTO);

            mockMvc.perform(get("/api/monitoreo/recursos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Río Bogotá"));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/recursos")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todos los recursos")
        void debeRetornarTodos() throws Exception {
            RecursoDTO r2 = new RecursoDTO(2L, "Lago Tota", TipoRecurso.Lago, "Boyacá", 5.56, -72.92);
            when(useCase.listarTodos()).thenReturn(List.of(recursoDTO, r2));

            mockMvc.perform(get("/api/monitoreo/recursos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nombre").value("Río Bogotá"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay recursos")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/monitoreo/recursos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/monitoreo/recursos/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminar(1L);

            mockMvc.perform(delete("/api/monitoreo/recursos/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminar(1L);
        }
    }
}
