package com.AquaBalance.events.infrastructure.web;

import com.AquaBalance.events.application.EventoDTO;
import com.AquaBalance.events.application.ports.in.GestionarEventoUseCase;
import com.AquaBalance.events.infrastructure.web.dto.EventoRequest;
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
@DisplayName("EventoController - Pruebas Unitarias")
class EventoControllerTest {

    @Mock  private GestionarEventoUseCase useCase;
    @InjectMocks private EventoController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private EventoDTO eventoDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        eventoDTO = new EventoDTO(1L, "Desbordamiento detectado", "Alta",
                LocalDateTime.of(2025, 3, 5, 14, 30), 1L, 1L);
    }

    private EventoRequest buildRequest() {
        EventoRequest req = new EventoRequest();
        req.setDescripcion("Desbordamiento detectado");
        req.setMagnitud("Alta");
        req.setIdContaminante(1L);
        req.setIdRecurso(1L);
        return req;
    }

    @Nested
    @DisplayName("POST /api/eventos")
    class Crear {

        @Test
        @DisplayName("Debe retornar 200 con el evento creado")
        void debeRetornar200() throws Exception {
            when(useCase.crear(any())).thenReturn(eventoDTO);

            mockMvc.perform(post("/api/eventos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.descripcion").value("Desbordamiento detectado"))
                    .andExpect(jsonPath("$.magnitud").value("Alta"));
        }
    }

    @Nested
    @DisplayName("GET /api/eventos")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar lista de eventos")
        void debeRetornarLista() throws Exception {
            EventoDTO e2 = new EventoDTO(2L, "Contaminación elevada", "Media", LocalDateTime.now(), 1L, 2L);
            when(useCase.listarTodos()).thenReturn(List.of(eventoDTO, e2));

            mockMvc.perform(get("/api/eventos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].descripcion").value("Desbordamiento detectado"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay eventos")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/eventos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/eventos/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar evento por ID")
        void debeRetornarPorId() throws Exception {
            when(useCase.buscarPorId(1L)).thenReturn(eventoDTO);

            mockMvc.perform(get("/api/eventos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.descripcion").value("Desbordamiento detectado"));
        }
    }

    @Nested
    @DisplayName("GET /api/eventos/recurso/{idRecurso}")
    class ListarPorRecurso {

        @Test
        @DisplayName("Debe retornar eventos del recurso")
        void debeRetornarPorRecurso() throws Exception {
            when(useCase.listarPorRecurso(1L)).thenReturn(List.of(eventoDTO));

            mockMvc.perform(get("/api/eventos/recurso/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].idRecurso").value(1L));
        }
    }

    @Nested
    @DisplayName("PUT /api/eventos/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar evento actualizado")
        void debeActualizar() throws Exception {
            EventoDTO actualizado = new EventoDTO(1L, "Nueva descripción", "Baja", LocalDateTime.now(), 1L, 1L);
            when(useCase.actualizar(eq(1L), any())).thenReturn(actualizado);

            mockMvc.perform(put("/api/eventos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descripcion").value("Nueva descripción"))
                    .andExpect(jsonPath("$.magnitud").value("Baja"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/eventos/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe retornar 204 al eliminar")
        void debeRetornar204() throws Exception {
            doNothing().when(useCase).eliminar(1L);

            mockMvc.perform(delete("/api/eventos/1"))
                    .andExpect(status().isNoContent());

            verify(useCase).eliminar(1L);
        }
    }
}
