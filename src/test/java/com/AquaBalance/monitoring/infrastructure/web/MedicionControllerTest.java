package com.AquaBalance.monitoring.infrastructure.web;

import com.AquaBalance.monitoring.application.MedicionDTO;
import com.AquaBalance.monitoring.application.ports.in.RegistrarMedicionUseCase;
import com.AquaBalance.monitoring.infrastructure.web.dto.MedicionRequest;
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
@DisplayName("MedicionController - Pruebas Unitarias")
class MedicionControllerTest {

    @Mock  private RegistrarMedicionUseCase useCase;
    @InjectMocks private MedicionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MedicionDTO medicionDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        medicionDTO = new MedicionDTO(1L, 7.0, 22.5, LocalDateTime.now(), 1L, 1L, 1L);
        medicionDTO.setNombreRecurso("Río Bogotá");
        medicionDTO.setNombreContaminante("Mercurio");
    }

    private MedicionRequest buildRequest() {
        MedicionRequest req = new MedicionRequest();
        req.setPh(7.0);
        req.setTemperatura(22.5);
        req.setIdUsuario(1L);
        req.setIdRecurso(1L);
        req.setIdContaminante(1L);
        return req;
    }

    @Nested
    @DisplayName("POST /api/monitoreo/mediciones")
    class Registrar {

        @Test
        @DisplayName("Debe retornar 201 con la medición registrada")
        void debeRetornar201() throws Exception {
            when(useCase.registrar(any())).thenReturn(medicionDTO);

            mockMvc.perform(post("/api/monitoreo/mediciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.ph").value(7.0))
                    .andExpect(jsonPath("$.nombreRecurso").value("Río Bogotá"));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/mediciones/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar medición por ID")
        void debeRetornarPorId() throws Exception {
            when(useCase.buscarPorId(1L)).thenReturn(medicionDTO);

            mockMvc.perform(get("/api/monitoreo/mediciones/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.temperatura").value(22.5));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/mediciones")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todas las mediciones")
        void debeRetornarTodas() throws Exception {
            MedicionDTO m2 = new MedicionDTO(2L, 6.5, 18.0, LocalDateTime.now(), 2L, 1L, 1L);
            when(useCase.listarTodos()).thenReturn(List.of(medicionDTO, m2));

            mockMvc.perform(get("/api/monitoreo/mediciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay mediciones")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/monitoreo/mediciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/monitoreo/mediciones/recurso/{idRecurso}")
    class ListarPorRecurso {

        @Test
        @DisplayName("Debe retornar mediciones del recurso")
        void debeRetornarPorRecurso() throws Exception {
            when(useCase.listarPorRecurso(1L)).thenReturn(List.of(medicionDTO));

            mockMvc.perform(get("/api/monitoreo/mediciones/recurso/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].ph").value(7.0));
        }
    }

    @Nested
    @DisplayName("PUT /api/monitoreo/mediciones/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar medición actualizada")
        void debeActualizar() throws Exception {
            MedicionDTO actualizada = new MedicionDTO(1L, 7.5, 24.0, LocalDateTime.now(), 1L, 1L, 1L);
            when(useCase.actualizar(eq(1L), any())).thenReturn(actualizada);

            mockMvc.perform(put("/api/monitoreo/mediciones/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ph").value(7.5));
        }
    }

    @Nested
    @DisplayName("DELETE /api/monitoreo/mediciones/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminar(1L);

            mockMvc.perform(delete("/api/monitoreo/mediciones/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminar(1L);
        }
    }
}
