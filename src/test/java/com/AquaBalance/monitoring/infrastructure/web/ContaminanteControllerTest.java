package com.AquaBalance.monitoring.infrastructure.web;

import com.AquaBalance.monitoring.application.ContaminanteDTO;
import com.AquaBalance.monitoring.application.ports.in.GestionarContaminanteUseCase;
import com.AquaBalance.monitoring.domain.NivelContaminante;
import com.AquaBalance.monitoring.infrastructure.web.dto.ContaminanteRequest;
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
@DisplayName("ContaminanteController - Pruebas Unitarias")
class ContaminanteControllerTest {

    @Mock  private GestionarContaminanteUseCase useCase;
    @InjectMocks private ContaminanteController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ContaminanteDTO contaminanteDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        contaminanteDTO = new ContaminanteDTO(1L, "Mercurio", 5.0, NivelContaminante.Alto, "Minería");
    }

    private ContaminanteRequest buildRequest() {
        ContaminanteRequest req = new ContaminanteRequest();
        req.setNombre("Mercurio");
        req.setCarga(5.0);
        req.setNivel(NivelContaminante.Alto);
        req.setFuenteOrigen("Minería");
        return req;
    }

    @Nested
    @DisplayName("POST /api/monitoreo/contaminantes")
    class Crear {

        @Test
        @DisplayName("Debe retornar 200 con el contaminante creado")
        void debeRetornar200() throws Exception {
            when(useCase.crear(any())).thenReturn(contaminanteDTO);

            mockMvc.perform(post("/api/monitoreo/contaminantes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Mercurio"))
                    .andExpect(jsonPath("$.nivel").value("Alto"));
        }
    }

    @Nested
    @DisplayName("PUT /api/monitoreo/contaminantes/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar contaminante actualizado")
        void debeActualizar() throws Exception {
            ContaminanteDTO actualizado = new ContaminanteDTO(1L, "Plomo", 3.0, NivelContaminante.Medio, "Industrial");
            when(useCase.actualizar(eq(1L), any())).thenReturn(actualizado);

            mockMvc.perform(put("/api/monitoreo/contaminantes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Plomo"))
                    .andExpect(jsonPath("$.nivel").value("Medio"));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/contaminantes/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar contaminante por ID")
        void debeRetornarPorId() throws Exception {
            when(useCase.buscarPorId(1L)).thenReturn(contaminanteDTO);

            mockMvc.perform(get("/api/monitoreo/contaminantes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Mercurio"));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/contaminantes")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todos los contaminantes")
        void debeRetornarTodos() throws Exception {
            ContaminanteDTO c2 = new ContaminanteDTO(2L, "Arsénico", 2.0, NivelContaminante.Bajo, "Agrícola");
            when(useCase.listarTodos()).thenReturn(List.of(contaminanteDTO, c2));

            mockMvc.perform(get("/api/monitoreo/contaminantes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nombre").value("Mercurio"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay contaminantes")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/monitoreo/contaminantes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/monitoreo/contaminantes/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminar(1L);

            mockMvc.perform(delete("/api/monitoreo/contaminantes/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminar(1L);
        }
    }
}
