package com.AquaBalance.notifications.infrastructure.web;

import com.AquaBalance.notifications.application.ports.in.GestionarNotificacionUseCase;
import com.AquaBalance.notifications.domain.Notificacion;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionController - Pruebas Unitarias")
class NotificacionControllerTest {

    @Mock  private GestionarNotificacionUseCase useCase;
    @InjectMocks private NotificacionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Nested
    @DisplayName("GET /api/notificaciones")
    class Listar {

        @Test
        @DisplayName("Debe retornar historial de notificaciones")
        void debeRetornarHistorial() throws Exception {
            Notificacion n1 = new Notificacion("ALERTA", "Nivel alto", "pH crítico", "ALTA");
            Notificacion n2 = new Notificacion("MEDICION", "Nueva medición", "pH registrado", "BAJA");
            when(useCase.obtenerHistorial()).thenReturn(List.of(n1, n2));

            mockMvc.perform(get("/api/notificaciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].tipo").value("ALERTA"))
                    .andExpect(jsonPath("$[1].tipo").value("MEDICION"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay notificaciones")
        void debeRetornarListaVacia() throws Exception {
            when(useCase.obtenerHistorial()).thenReturn(List.of());

            mockMvc.perform(get("/api/notificaciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/notificaciones/no-leidas")
    class ContarNoLeidas {

        @Test
        @DisplayName("Debe retornar cantidad de no leidas")
        void debeRetornarCantidad() throws Exception {
            when(useCase.contarNoLeidas()).thenReturn(5L);

            mockMvc.perform(get("/api/notificaciones/no-leidas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cantidad").value(5));
        }

        @Test
        @DisplayName("Debe retornar cero cuando no hay no leídas")
        void debeRetornarCero() throws Exception {
            when(useCase.contarNoLeidas()).thenReturn(0L);

            mockMvc.perform(get("/api/notificaciones/no-leidas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cantidad").value(0));
        }
    }

    @Nested
    @DisplayName("PUT /api/notificaciones/marcar-leidas")
    class MarcarTodasLeidas {

        @Test
        @DisplayName("Debe retornar 200 al marcar todas como leídas")
        void debeRetornar200() throws Exception {
            doNothing().when(useCase).marcarTodasLeidas();

            mockMvc.perform(put("/api/notificaciones/marcar-leidas"))
                    .andExpect(status().isOk());

            verify(useCase).marcarTodasLeidas();
        }
    }

    @Nested
    @DisplayName("PUT /api/notificaciones/{id}/leer")
    class MarcarLeida {

        @Test
        @DisplayName("Debe retornar 200 al marcar una notificación como leída")
        void debeRetornar200() throws Exception {
            doNothing().when(useCase).marcarLeida(1L);

            mockMvc.perform(put("/api/notificaciones/1/leer"))
                    .andExpect(status().isOk());

            verify(useCase).marcarLeida(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/notificaciones/test")
    class EnviarPrueba {

        @Test
        @DisplayName("Debe retornar 200 al enviar notificación de prueba")
        void debeRetornar200() throws Exception {
            doNothing().when(useCase).notificar(any());

            Notificacion notificacion = new Notificacion("PRUEBA", "Test", "Mensaje de prueba", "BAJA");

            mockMvc.perform(post("/api/notificaciones/test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificacion)))
                    .andExpect(status().isOk());

            verify(useCase).notificar(any());
        }
    }
}
