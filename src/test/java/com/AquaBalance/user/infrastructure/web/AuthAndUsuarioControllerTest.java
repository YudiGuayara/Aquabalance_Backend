package com.AquaBalance.user.infrastructure.web;

import com.AquaBalance.user.application.AuthService;
import com.AquaBalance.user.application.UserDTO;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.GestionarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
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

// ════════════════════════════════════════════════════════════════════
// AuthController
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    @Mock  private AuthService authService;
    @InjectMocks private AuthController controller;

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
    @DisplayName("POST /api/auth/registro")
    class Registro {

        @Test
        @DisplayName("Debe retornar 200 con el token de acceso al registrar")
        void debeRetornarTokenAlRegistrar() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setNombre("Laura Gómez");
            req.setEmail("laura@aqua.com");
            req.setPassword("secret123");
            req.setRol("Operador");

            AuthResponse response = new AuthResponse(1L, "jwt-token-123", "laura@aqua.com", "Laura Gómez", "Operador");
            when(authService.registrar(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/registro")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-123"))
                    .andExpect(jsonPath("$.email").value("laura@aqua.com"))
                    .andExpect(jsonPath("$.nombre").value("Laura Gómez"))
                    .andExpect(jsonPath("$.rol").value("Operador"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("Debe retornar 200 con token al hacer login")
        void debeRetornarTokenAlLogin() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("laura@aqua.com");
            req.setPassword("secret123");

            AuthResponse response = new AuthResponse(1L, "jwt-token-456", "laura@aqua.com", "Laura Gómez", "Operador");
            when(authService.login(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-456"))
                    .andExpect(jsonPath("$.email").value("laura@aqua.com"));
        }
    }
}


// ════════════════════════════════════════════════════════════════════
// UsuarioController
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Pruebas Unitarias")
class UsuarioControllerTest {

    @Mock  private BuscarUsuarioUseCase    buscarUseCase;
    @Mock  private GestionarUsuarioUseCase gestionarUseCase;
    @Mock  private RegistrarUsuarioUseCase registrarUseCase;
    @InjectMocks private UsuarioController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Usuario usuarioActivo;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        usuarioActivo = new Usuario(1L, "Laura Gómez", "laura@aqua.com", "hashed", Rol.Operador);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setNombre("Laura Gómez");
        userDTO.setEmail("laura@aqua.com");
        userDTO.setRol("Operador");
        userDTO.setActivo(true);
        userDTO.setFechaCreacion(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/usuarios")
    class Listar {

        @Test
        @DisplayName("Debe retornar lista de usuarios")
        void debeRetornarLista() throws Exception {
            Usuario u2 = new Usuario(2L, "Carlos Admin", "carlos@aqua.com", "hashed2", Rol.Administrador);
            when(gestionarUseCase.listarTodos()).thenReturn(List.of(usuarioActivo, u2));

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nombre").value("Laura Gómez"))
                    .andExpect(jsonPath("$[1].rol").value("Administrador"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay usuarios")
        void debeRetornarListaVacia() throws Exception {
            when(gestionarUseCase.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar usuario por ID")
        void debeRetornarPorId() throws Exception {
            when(buscarUseCase.buscarPorId(1L)).thenReturn(usuarioActivo);

            mockMvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Laura Gómez"))
                    .andExpect(jsonPath("$.email").value("laura@aqua.com"));
        }
    }

    @Nested
    @DisplayName("POST /api/usuarios")
    class Crear {

        @Test
        @DisplayName("Debe retornar 200 con el usuario creado")
        void debeRetornar200() throws Exception {
            when(registrarUseCase.registrar(any())).thenReturn(usuarioActivo);

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Laura Gómez"))
                    .andExpect(jsonPath("$.email").value("laura@aqua.com"));
        }
    }

    @Nested
    @DisplayName("PUT /api/usuarios/{id}")
    class Actualizar {

        @Test
        @DisplayName("Debe retornar usuario actualizado")
        void debeActualizar() throws Exception {
            Usuario actualizado = new Usuario(1L, "Laura Actualizada", "laura@aqua.com", "hashed", Rol.Administrador);
            when(gestionarUseCase.actualizar(eq(1L), any())).thenReturn(actualizado);

            mockMvc.perform(put("/api/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Laura Actualizada"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/usuarios/{id}/activar")
    class Activar {

        @Test
        @DisplayName("Debe activar usuario y retornar 200")
        void debeActivar() throws Exception {
            doNothing().when(gestionarUseCase).activarUsuario(1L);

            mockMvc.perform(patch("/api/usuarios/1/activar"))
                    .andExpect(status().isOk());

            verify(gestionarUseCase).activarUsuario(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/usuarios/{id}/desactivar")
    class Desactivar {

        @Test
        @DisplayName("Debe desactivar usuario y retornar 200")
        void debeDesactivar() throws Exception {
            doNothing().when(gestionarUseCase).desactivarUsuario(1L);

            mockMvc.perform(patch("/api/usuarios/1/desactivar"))
                    .andExpect(status().isOk());

            verify(gestionarUseCase).desactivarUsuario(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/usuarios/{id}")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar físicamente y retornar 204")
        void debeEliminarAlEliminar() throws Exception {
            doNothing().when(gestionarUseCase).eliminarUsuario(1L);

            mockMvc.perform(delete("/api/usuarios/1"))
                    .andExpect(status().isNoContent());

            verify(gestionarUseCase).eliminarUsuario(1L);
        }
    }
}