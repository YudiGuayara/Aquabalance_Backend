package com.AquaBalance.events.infrastructure.web;

import com.AquaBalance.events.application.AlertaDTO;
import com.AquaBalance.events.application.ports.in.GestionarAlertaUseCase;
import com.AquaBalance.events.domain.NivelAlerta;
import com.AquaBalance.events.infrastructure.web.dto.AlertaRequest;
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
@DisplayName("AlertaController - Pruebas Unitarias")
class AlertaControllerTest {

    @Mock  private GestionarAlertaUseCase useCase;
    @InjectMocks private AlertaController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AlertaDTO alertaDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        alertaDTO = new AlertaDTO(1L, LocalDateTime.of(2025, 4, 20, 8, 0),
                NivelAlerta.Roja, "Nivel crítico", 1L, 1L);
    }

    private AlertaRequest buildRequest() {
        AlertaRequest req = new AlertaRequest();
        req.setNivel(NivelAlerta.Roja);
        req.setMensaje("Nivel crítico");
        req.setIdUsuario(1L);
        req.setIdEvento(1L);
        return req;
    }

    @Nested
    @DisplayName("POST /api/alertas")
    class Crear {

        @Test
        @DisplayName("Debe retornar 200 con la alerta creada")
        void debeRetornar200() throws Exception {
            when(useCase.crear(any())).thenReturn(alertaDTO);

            mockMvc.perform(post("/api/alertas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nivel").value("Roja"))
                    .andExpect(jsonPath("$.mensaje").value("Nivel crítico"));

            verify(useCase).crear(any());
        }
    }

    @Nested
    @DisplayName("GET /api/alertas")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar lista de alertas")
        void debeRetornarLista() throws Exception {
            AlertaDTO a2 = new AlertaDTO(2L, LocalDateTime.now(), NivelAlerta.Amarilla, "Advertencia", 2L, 2L);
            when(useCase.listarTodos()).thenReturn(List.of(alertaDTO, a2));

            mockMvc.perform(get("/api/alertas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[1].nivel").value("Amarilla"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay alertas")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/alertas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/alertas/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar alerta por ID")
        void debeRetornarPorId() throws Exception {
            when(useCase.buscarPorId(1L)).thenReturn(alertaDTO);

            mockMvc.perform(get("/api/alertas/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nivel").value("Roja"));
        }
    }

    @Nested
    @DisplayName("GET /api/alertas/nivel/{nivel}")
    class ListarPorNivel {

        @Test
        @DisplayName("Debe retornar alertas filtradas por nivel")
        void debeRetornarPorNivel() throws Exception {
            when(useCase.listarPorNivel(NivelAlerta.Roja)).thenReturn(List.of(alertaDTO));

            mockMvc.perform(get("/api/alertas/nivel/Roja"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nivel").value("Roja"));
        }
    }

    @Nested
    @DisplayName("GET /api/alertas/evento/{idEvento}")
    class ListarPorEvento {

        @Test
        @DisplayName("Debe retornar alertas del evento")
        void debeRetornarPorEvento() throws Exception {
            when(useCase.listarPorEvento(1L)).thenReturn(List.of(alertaDTO));

            mockMvc.perform(get("/api/alertas/evento/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("PUT /api/alertas/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar alerta actualizada")
        void debeActualizar() throws Exception {
            AlertaDTO actualizada = new AlertaDTO(1L, LocalDateTime.now(), NivelAlerta.Verde, "Normalizado", 1L, 1L);
            when(useCase.actualizar(eq(1L), any())).thenReturn(actualizada);

            mockMvc.perform(put("/api/alertas/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nivel").value("Verde"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/alertas/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminar(1L);

            mockMvc.perform(delete("/api/alertas/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminar(1L);
        }
    }
}
